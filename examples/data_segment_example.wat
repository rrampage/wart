;; From spec repo: https://github.com/WebAssembly/bulk-memory-operations/blob/master/proposals/bulk-memory-operations/Overview.md
(import "a" "global" (global i32))  ;; global 0
(memory 1)
(data (i32.const 0) "hello")   ;; data segment 0, is active so always copied
(data "goodbye")               ;; data segment 1, is passive

(func $start
  (if (global.get 0)

    ;; copy data segment 1 into memory 0 (the 0 is implicit)
    (memory.init 1
      (i32.const 16)    ;; target offset
      (i32.const 0)     ;; source offset
      (i32.const 7))    ;; length

    ;; The memory used by this segment is no longer needed, so this segment can
    ;; be dropped.
    (data.drop 1))
)