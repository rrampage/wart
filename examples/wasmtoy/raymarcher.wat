;; From https://c0de517e.com/004_wasmtoy/main.html
;; Documentation links:
;; Format, basics: https://developer.mozilla.org/en-US/docs/WebAssembly/Understanding_the_text_format
;; All instructions: https://webassembly.github.io/spec/core/text/instructions.html
;; By example: https://learnxinyminutes.com/docs/wasm/ https://openhome.cc/eGossip/WebAssembly/index.html
;; Branching and blocks: https://samrat.me/posts/2020-03-29-webassembly-control-instr-examples/
;; Websites: http://mbebenita.github.io/WasmExplorer/ https://wasdk.github.io/WasmFiddle/ https://webassembly.github.io/wabt/demo/wat2wasm/

(module ;; In WASM, we always need a module

;; A memory block is 64k in wasm (can grow, it specifies only the minimum size). Memory can be imported from javascript, exported to it, or just used locally.
;; The following memory will be allocated by js and imported. There can be only one memory block per module, and it's what load/store instructions refer to.

(import "js" "mem" (memory 5))

;; Memory Map - this is how this module will communicate with the system!
;; 0-63999 = 320x200 8bit palette image
;; 64000-65023 = 256 RGBA8 palette entries

;; Like memory, globals could be imported/exported to JS, but we don't need that now.
;; Note: All "$something" identifiers in wasm are optional string IDs, if they are not present, webassembly still allows to reference to objects by index.
(global $frame (mut i32) (i32.const 0))

;; Wasm is hybrid - it allows locals, but the syntax was relaxed to allow for a stack-machine like encoding.
;; The instruction syntax is also hybrid. Instructions can be written without operands, using the stack,
;; or can be written using s-expressions, directly specifying the parameters.
;; The result is the same, parameters are still pushed to the stack and popped
;; E.g.
;;  i32.const 333
;;  i32.const 333
;;  i32.add
;; is the same as:
;;  (i32.add (i32.const 333) (i32.const 333))
;;
;; That said, it is noticeable how it was not designed primarly as a stack machine as it lacks any stack manipulation operators.
;; E.g. it does not have a "dup" instruction for replicating a value on the stack, or swap/rot etc...

(global $zero f32 (f32.const 0))
(global $one f32 (f32.const 1))
(global $half f32 (f32.const 0.5))

;; Let's define our main function.
;; Function syntax -> ( func (param type)... (result type) (local type)... <body> )
(func $wasm_main ;; This raymarcher originally was from: https://www.shadertoy.com/view/lsXyWN
    (local $x f32) (local $y f32)
    (local $idx i32)
    (local $Rx f32) (local $Ry f32) (local $Rz f32)
    (local $Qx f32) (local $Qy f32) (local $Qz f32)
    (local $d f32) (local $s f32) (local $o f32)

    (local.set $idx (i32.const 0))
    (local.set $y (f32.const -0.3125)) ;; 100./320.
    (loop $yloop ;; for each row
      (local.set $x (f32.const -0.5)) ;; 160./320.
      (loop $xloop ;; for each pixel in a row
          (local.set $Qx (global.get $zero)) ;; world position
          (local.set $Qy (global.get $zero))
          (local.set $Qz (f32.mul (f32.convert_i32_u (global.get $frame)) (f32.const 0.02)))
          (local.set $o (global.get $one))
          (loop $dloop ;; iterate the distance raymarcher
            (local.set $s (global.get $one))
            (local.set $d (global.get $zero))
            (loop $sloop ;; compute the distance function for the sponge
                (local.set $Rx (f32.add (f32.mul (local.get $Qx) (local.get $s)) (global.get $one)))
                (local.set $Ry (f32.add (f32.mul (local.get $Qy) (local.get $s)) (global.get $one)))
                (local.set $Rz (f32.add (f32.mul (local.get $Qz) (local.get $s)) (global.get $one)))
                (local.set $Rx (f32.abs (f32.sub (f32.sub (local.get $Rx) (f32.div (f32.floor (f32.mul (local.get $Rx) (global.get $half)) ) (global.get $half) )) (global.get $one))))
                (local.set $Ry (f32.abs (f32.sub (f32.sub (local.get $Ry) (f32.div (f32.floor (f32.mul (local.get $Ry) (global.get $half)) ) (global.get $half) )) (global.get $one))))
                (local.set $Rz (f32.abs (f32.sub (f32.sub (local.get $Rz) (f32.div (f32.floor (f32.mul (local.get $Rz) (global.get $half)) ) (global.get $half) )) (global.get $one))))
                (local.set $d (f32.max (local.get $d) (f32.div (f32.sub (f32.const 0.33) (f32.min (f32.min (f32.max (local.get $Ry) (local.get $Rz)) (f32.max (local.get $Rz) (local.get $Rx)) ) (f32.max (local.get $Rx) (local.get $Ry)) ) ) (local.get $s))))
                (local.set $s (f32.mul (local.get $s) (f32.const 3.0)))
                (br_if $sloop (f32.lt (local.get $s) (f32.const 21.0)))
            )
            (local.set $Qx (f32.add (local.get $Qx) (f32.mul (local.get $x) (local.get $d)))) ;; advance the position
            (local.set $Qy (f32.add (local.get $Qy) (f32.mul (local.get $y) (local.get $d))))
            (local.set $Qz (f32.add (local.get $Qz) (local.get $d)))
            (local.set $o (f32.sub (local.get $o) (f32.const 0.01))) ;; this will be the output color, more steps we do, the darker
            (br_if $dloop (i32.and (f32.gt (local.get $d) (f32.const 0.001)) (f32.gt (local.get $o) (global.get $zero)))) ;; stop if we found the surface, or if it's too dark

            (i32.store8 (local.get $idx) (i32.trunc_sat_f32_u (f32.mul (local.get $o) (f32.const 255.0)))) ;; store the color
            (local.set $idx (i32.add (local.get $idx) (i32.const 1)))
            (local.set $x (f32.add (local.get $x) (f32.const 0.003125))) ;; 1./320.
            (br_if $xloop (f32.lt (local.get $x) (global.get $half))) ;; 160./320.
          )
          (local.set $y (f32.add (local.get $y) (f32.const 0.003125))) ;; 1./320.
          (br_if $yloop (i32.lt_s (local.get $idx) (i32.const 64000)))
      )
    )
    (global.set $frame (i32.add (global.get $frame) (i32.const 1)))
)
  (func $palette_to_rgba8 (;0;)
    (local $idx (;0;) i32) (local $val (;1;) i32)
    i32.const 0
    local.set $idx
    loop $label0
      local.get $idx
      i32.const 4
      i32.mul
      local.get $idx
      i32.load8_u
      i32.const 4
      i32.mul
      i32.load offset=64000
      i32.store offset=65536
      local.get $idx
      i32.const 1
      i32.add
      local.tee $idx
      i32.const 64000
      i32.le_s
      br_if $label0
    end $label0
  )

;; Let's export the function to JS. This can be done "inline" together with the function
;; definition, but this way it's a bit less messy...
(export "wasm_main" (func $wasm_main))
(export "palette_to_rgba8" (func $palette_to_rgba8))
) ;; end of module