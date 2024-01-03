(module
  (type (;0;) (func (param i32 i32) (result i32)))
  (func (;0;) (type 0) (param i32 i32) (result i32)
    local.get 0
    local.get 1
    i32.mul)
  (func (;1;) (type 0) (param i32 i32) (result i32)
    local.get 0
    local.get 1
    i32.div_s)
  (table (;0;) 2 funcref)
  (export "math_tbl" (table 0))
  (elem (;0;) (i32.const 0) func 0 1))