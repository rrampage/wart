(module
      (func (export "simple-loop") (param i64) (result i64)
        (local.set 0 (i64.const 111))
        (block
          (loop
            (if
              (i64.eq (local.get 0) (i64.const 111))
              (then (br 2))
              (else
                (local.set 0 (i64.const 1111))
                (br 1)
              )
            )
            (br 0)
          )
        )
        (local.get 0)
      )

      (func (export "break-br_if-num-num") (param i32) (result i32 i64)
          (drop (drop (br_if 0 (i32.const 50) (i64.const 51) (local.get 0))))
          (i32.const 51) (i64.const 52)
      )
)