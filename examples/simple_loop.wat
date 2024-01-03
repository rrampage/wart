(module
  (type (;0;) (func))
  (type (;1;) (func (param i32) (result i32)))
  (import "env" "memory" (memory (;0;) 2))
  (func $__wasm_call_ctors (type 0))
  (func $fun1 (type 1) (param i32) (result i32)
    (local i32 i32 i32 i32 i32)
    block  ;; label = @1
      local.get 0
      i32.const 9
      i32.gt_s
      br_if 0 (;@1;)
      local.get 0
      i32.const 1
      i32.shl
      return
    end
    block  ;; label = @1
      local.get 0
      i32.const 100
      i32.gt_s
      br_if 0 (;@1;)
      local.get 0
      i32.const -2
      i32.add
      local.set 1
      i32.const 10
      local.set 2
      i32.const 0
      local.set 3
      i32.const 0
      local.set 4
      loop  ;; label = @2
        block  ;; label = @3
          local.get 4
          local.get 0
          i32.ge_s
          br_if 0 (;@3;)
          local.get 3
          local.get 2
          local.get 4
          local.get 4
          i32.const -1
          i32.xor
          local.get 0
          i32.add
          local.tee 5
          local.get 4
          i32.const 1
          i32.add
          i32.mul
          i32.add
          local.get 5
          i64.extend_i32_u
          local.get 1
          local.get 4
          i32.sub
          i64.extend_i32_u
          i64.mul
          i64.const 1
          i64.shr_u
          i32.wrap_i64
          i32.add
          i32.mul
          i32.add
          local.set 3
          local.get 0
          local.set 4
        end
        local.get 2
        i32.const 98
        i32.lt_u
        local.set 5
        local.get 2
        i32.const 2
        i32.add
        local.set 2
        local.get 5
        br_if 0 (;@2;)
      end
      local.get 3
      return
    end
    local.get 0
    i32.const 1600
    i32.mul)
  (table (;0;) 1 1 funcref)
  (global (;0;) (mut i32) (i32.const 66560))
  (global (;1;) i32 (i32.const 1024))
  (global (;2;) i32 (i32.const 1024))
  (global (;3;) i32 (i32.const 1024))
  (global (;4;) i32 (i32.const 66560))
  (global (;5;) i32 (i32.const 0))
  (global (;6;) i32 (i32.const 1))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "fun1" (func $fun1))
  (export "__dso_handle" (global 1))
  (export "__data_end" (global 2))
  (export "__global_base" (global 3))
  (export "__heap_base" (global 4))
  (export "__memory_base" (global 5))
  (export "__table_base" (global 6)))
