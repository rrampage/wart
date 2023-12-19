;; From https://ashen.earth/_next/static/scripts/wasm-life-2/game.wat
;; Blog post: https://ashen.earth/wasm-game-of-life-2
(module
  (memory (export "shared_memory") 1)

  (global $boardWidth (mut i32) (i32.const 0))
  (global $boardHeight (mut i32) (i32.const 0))
  (global $boardBufferLength (mut i32) (i32.const 0))
  (global $currentBuffer (mut i32) (i32.const -1))

  (func (export "initializeBoard") (param $width i32) (param $height i32)
    ;; Store width and height for later
    (global.set $boardWidth (local.get $width))
    (global.set $boardHeight (local.get $height))

    ;; Compute total cells per board
    local.get $width
    local.get $height
    i32.mul
    global.set $boardBufferLength

    ;; Request enough memory for both boards
    global.get $boardBufferLength
    i32.const 2
    i32.mul
    call $growMemoryForBoards

    ;; Set current board
    (global.set $currentBuffer (i32.const 0))
  )

  (func (export "getPagesUsed") (result i32)
    memory.size
  )

  (func $growMemoryForBoards (param $totalBytes i32)
    (local $targetPages i32)

    ;; figure out target page size
    local.get $totalBytes
    i32.const 1
    i32.sub
    i32.const 65536 ;; size of a page
    i32.div_u
    i32.const 1
    i32.add

    ;; get difference
    memory.size
    i32.sub

    ;; grow
    memory.grow
    drop ;; ignore result, we're gonna crash anyways
    ;; perhaps we should have a way to report errors back to JS next time
  )

  (func $getBoardPtr (export "getBoardPointer") (result i32)
    global.get $currentBuffer
    global.get $boardBufferLength
    i32.mul
  )

  (func (export "getBoardLength") (result i32)
    global.get $boardBufferLength
  )

  (func $swapBoards
    global.get $currentBuffer
    i32.eqz
    global.set $currentBuffer
  )

  (func $positionOutOfRange (param $position i32) (param $max i32) (result i32)
    local.get $position
    i32.const 0
    i32.lt_s

    local.get $position
    local.get $max
    i32.ge_s

    i32.or
  )

  (func $getIndexForPosition (param $row i32) (param $column i32) (result i32)
    (local $result i32)
    local.get $row
    global.get $boardHeight
    call $positionOutOfRange

    local.get $column
    global.get $boardWidth
    call $positionOutOfRange

    i32.and
    if
      i32.const -1
      return
    end

    global.get $boardWidth
    local.get $row
    i32.mul
    local.get $column
    i32.add
  )

  (func $getValueAtPosition (export "getValueAtPosition") (param $row i32) (param $column i32) (result i32)
    (local $position i32)
    local.get $row
    local.get $column
    call $getIndexForPosition

    local.tee $position
    i32.const 0
    i32.lt_s

    if
      i32.const 0
      return
    end

    local.get $position

    call $getBoardPtr
    i32.add
    i32.load8_u
  )

  (func $setValueAtPosition (export "setValueAtPosition") (param $row i32) (param $column i32) (param $value i32)
    (local $position i32)
    local.get $row
    local.get $column
    call $getIndexForPosition

    local.tee $position
    i32.const 0
    i32.lt_s

    if
      return
    end

    local.get $position

    call $getBoardPtr
    i32.add
    local.get $value
    i32.store8
  )

  (func $setNewValueAtPosition (param $row i32) (param $column i32) (param $value i32)
    (local $position i32)
    local.get $row
    local.get $column
    call $getIndexForPosition

    local.tee $position
    i32.const 0
    i32.lt_s

    if
      return
    end

    global.get $boardBufferLength
    call $getBoardPtr
    i32.xor
    local.get $position
    i32.add
    local.get $value
    i32.store8
  )

  (func $getNewValueAtPosition (param $row i32) (param $column i32) (result i32)
    (local $count i32)
    (local $current i32)
    local.get $row
    i32.const 1
    i32.lt_u

    local.get $row
    global.get $boardHeight
    i32.const 1
    i32.sub
    i32.ge_u

    local.get $column
    i32.const 1
    i32.lt_u

    local.get $column
    global.get $boardWidth
    i32.const 1
    i32.sub
    i32.ge_u

    ;; if any of the boundary conditions are true
    i32.or
    i32.or
    i32.or

    if (result i32 i32)
      local.get $row
      local.get $column
      call $getNeighborCountBoundary
    else
      local.get $row
      local.get $column
      call $getNeighborCountCenter
    end

    call $getOutcomeFromCountAndCurrent
  )

  (func $getNeighborCountBoundary (param $row i32) (param $column i32) (result i32 i32)
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

    ;; sum neightbor count
    i32.add
    i32.add
    i32.add
    i32.add
    i32.add
    i32.add
    i32.add

    ;; get current
    local.get $row
    local.get $column
    call $getValueAtPosition
  )

  (func $getNeighborCountCenter (param $row i32) (param $column i32) (result i32 i32) ;; neighborCount, currentValue
    (local $origIndex i32)
    (local $rowAbove i32)
    (local $rowCenter i32)
    (local $rowBelow i32)

    ;; store current cell's memory position
    call $getBoardPtr
    local.get $row
    local.get $column
    call $getIndexForPosition
    i32.add
    local.tee $origIndex

    ;; load the three rows
    i32.const 1
    i32.sub
    i32.load
    local.set $rowCenter

    local.get $origIndex
    i32.const 1
    i32.sub
    global.get $boardWidth
    i32.sub
    i32.load
    local.set $rowAbove

    local.get $origIndex
    i32.const 1
    i32.sub
    global.get $boardWidth
    i32.add
    i32.load
    local.set $rowBelow

    ;; count the number of alive neighbors in each row
    local.get $rowAbove
    i32.const 0x00_01_01_01
    i32.and
    i32.popcnt

    local.get $rowCenter
    i32.const 0x00_01_00_01
    i32.and
    i32.popcnt


    local.get $rowBelow
    i32.const 0x00_01_01_01
    i32.and
    i32.popcnt

    ;; sum neighbor count
    i32.add
    i32.add

    ;; get current
    local.get $rowCenter
    i32.const 0x00_00_01_00
    i32.and
    i32.popcnt
  )

  (func $getOutcomeFromCountAndCurrent (param $neighborCount i32) (param $current i32) (result i32)
    ;; get sentinal value for current count
    i32.const 1
    local.get $neighborCount
    i32.shl

    ;; get value mask for current state
    i32.const 0xC ;;alive mask (1100, 3 or 2 neighbors)
    i32.const 0x8 ;;dead mask  (1000, exactly three neighbors)
    local.get $current
    select

    ;; mask out to see if we still have a bit
    i32.and
    i32.popcnt
  )

  (func $tick (export "tick")
    (local $row i32)
    (local $column i32)
    (local $value i32)

    i32.const 0
    local.set $row

    loop $rows

      ;; start at the beginning of a row
      i32.const 0
      local.set $column

      ;; for every column in the row
      loop $columns

        ;; compute new value
        local.get $row
        local.get $column
        call $getNewValueAtPosition
        local.set $value

        ;; place in next board
        local.get $row
        local.get $column
        local.get $value
        call $setNewValueAtPosition

        ;; increment column
        local.get $column
        i32.const 1
        i32.add
        local.tee $column

        ;; loop back if less than width
        global.get $boardWidth
        i32.lt_s
        br_if $columns
      end

      ;;increment row
      local.get $row
      i32.const 1
      i32.add
      local.tee $row

      ;; loop back if less than height
      global.get $boardHeight
      i32.lt_s
      br_if $rows
    end

    ;; swap to the new board
    call $swapBoards
  )
)