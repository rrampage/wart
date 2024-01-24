(;
    Game of Life written in pure WASM
    From https://ashen.earth/wasm-game-of-life-1
;)
(module
  (type (;0;) (func (param i32 i32)))
  (type (;1;) (func (result i32)))
  (type (;2;) (func (param i32)))
  (type (;3;) (func))
  (type (;4;) (func (param i32 i32 i32) (result i32)))
  (type (;5;) (func (param i32 i32) (result i32)))
  (type (;6;) (func (param i32 i32 i32)))
  (func (;0;) (type 0) (param $width i32) (param $height i32)
    local.get $width
    global.set 0
    local.get $height
    global.set 1
    local.get $width
    local.get $height
    i32.mul
    global.set 2
    global.get 2
    i32.const 2
    i32.mul
    call $growMemoryForBoards
    i32.const 0
    global.set 3
    global.get 2
    global.set 4
    i32.const 0
    global.set 5)
  (func (;1;) (type 1) (result i32)
    memory.size)
  (func $growMemoryForBoards (type 2) (param $totalBytes i32)
    (local $targetPages i32)
    local.get $totalBytes
    i32.const 1
    i32.sub
    i32.const 65536
    i32.div_u
    i32.const 1
    i32.add
    memory.size
    i32.sub
    memory.grow
    drop)
  (func $getBoardPtr (type 1) (result i32)
    global.get 5
    i32.eqz
    if (result i32)  ;; label = @1
      global.get 3
    else
      global.get 4
    end)
  (func $swapBoards (type 3)
    global.get 5
    i32.eqz
    if (result i32)  ;; label = @1
      i32.const 1
    else
      i32.const 0
    end
    global.set 5)
  (func $positionInRange (type 4) (param $position i32) (param $min i32) (param $max i32) (result i32)
    local.get $position
    local.get $min
    i32.lt_s
    if  ;; label = @1
      i32.const 0
      return
    end
    local.get $position
    local.get $max
    i32.ge_s
    if  ;; label = @1
      i32.const 0
      return
    end
    i32.const 1
    return)
  (func $getIndexForPosition (type 5) (param $row i32) (param $column i32) (result i32)
    local.get $row
    i32.const 0
    global.get 1
    call $positionInRange
    local.get $column
    i32.const 0
    global.get 0
    call $positionInRange
    i32.and
    i32.eqz
    if  ;; label = @1
      i32.const -1
      return
    end
    global.get 0
    local.get $row
    i32.mul
    local.get $column
    i32.add)
  (func $getValueAtPosition (type 5) (param $row i32) (param $column i32) (result i32)
    (local $position i32)
    local.get $row
    local.get $column
    call $getIndexForPosition
    local.tee $position
    i32.const 0
    i32.lt_s
    if  ;; label = @1
      i32.const 0
      return
    end
    local.get $position
    call $getBoardPtr
    i32.add
    i32.load8_u)
  (func $setValueAtPosition (type 6) (param $row i32) (param $column i32) (param $value i32)
    (local $position i32)
    local.get $row
    local.get $column
    call $getIndexForPosition
    local.tee $position
    i32.const 0
    i32.lt_s
    if  ;; label = @1
      return
    end
    local.get $position
    call $getBoardPtr
    i32.add
    local.get $value
    i32.store8)
  (func $getNewValueAtPosition (type 5) (param $row i32) (param $column i32) (result i32)
    (local $count i32)
    local.get $row
    i32.const 1
    i32.sub
    local.get $column
    call $getValueAtPosition
    local.get $row
    i32.const 1
    i32.add
    local.get $column
    call $getValueAtPosition
    local.get $row
    local.get $column
    i32.const 1
    i32.sub
    call $getValueAtPosition
    local.get $row
    local.get $column
    i32.const 1
    i32.add
    call $getValueAtPosition
    local.get $row
    i32.const 1
    i32.sub
    local.get $column
    i32.const 1
    i32.sub
    call $getValueAtPosition
    local.get $row
    i32.const 1
    i32.add
    local.get $column
    i32.const 1
    i32.sub
    call $getValueAtPosition
    local.get $row
    i32.const 1
    i32.sub
    local.get $column
    i32.const 1
    i32.add
    call $getValueAtPosition
    local.get $row
    i32.const 1
    i32.add
    local.get $column
    i32.const 1
    i32.add
    call $getValueAtPosition
    i32.add
    i32.add
    i32.add
    i32.add
    i32.add
    i32.add
    i32.add
    local.tee $count
    i32.const 3
    i32.eq
    if  ;; label = @1
      i32.const 1
      return
    end
    local.get $row
    local.get $column
    call $getValueAtPosition
    i32.eqz
    if  ;; label = @1
      i32.const 0
      return
    end
    local.get $count
    i32.const 2
    i32.eq
    if  ;; label = @1
      i32.const 1
      return
    end
    i32.const 0
    return)
  (func $tick (type 3)
    (local $row i32) (local $column i32) (local $value i32)
    i32.const 0
    local.set $row
    loop  ;; label = @1
      i32.const 0
      local.set $column
      loop  ;; label = @2
        local.get $row
        local.get $column
        call $getNewValueAtPosition
        local.set $value
        call $swapBoards
        local.get $row
        local.get $column
        local.get $value
        call $setValueAtPosition
        call $swapBoards
        local.get $column
        i32.const 1
        i32.add
        local.tee $column
        global.get 0
        i32.lt_s
        br_if 0 (;@2;)
      end
      local.get $row
      i32.const 1
      i32.add
      local.tee $row
      global.get 1
      i32.lt_s
      br_if 0 (;@1;)
    end
    call $swapBoards)
  (memory (;0;) 1)
  (global (;0;) (mut i32) (i32.const 0))
  (global (;1;) (mut i32) (i32.const 0))
  (global (;2;) (mut i32) (i32.const 0))
  (global (;3;) (mut i32) (i32.const -1))
  (global (;4;) (mut i32) (i32.const -1))
  (global (;5;) (mut i32) (i32.const -1))
  (export "shared_memory" (memory 0))
  (export "initializeBoard" (func 0))
  (export "getPagesUsed" (func 1))
  (export "getValueAtPosition" (func $getValueAtPosition))
  (export "setValueAtPosition" (func $setValueAtPosition))
  (export "tick" (func $tick)))
