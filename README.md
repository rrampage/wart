# A simple WASM interpreter in multiple languages

Inspired by [David Beazley's mindblowing talk](https://www.youtube.com/watch?v=r-A78RgMhZU) on live-coding [a WASM interpreter](https://gist.github.com/dabeaz/7d8838b54dba5006c58a40fc28da9d5a)


WASM opcode references:
- [Opcode table](https://pengowray.github.io/wasm-ops/)
- [MDN WASM Instruction reference](https://developer.mozilla.org/en-US/docs/WebAssembly/Reference)

## wag - Golang WASM interpreter
```bash
cd wag
go mod tidy
go test -v
```

### Currently implemented
- Works with i32
- Support for load/store
- Support for arithmetic
- Support for simple functions

### TODO:
- Branching
- Blocks
- Support i64, f32, f64
- Imports
- Run on simple wasm files

## waja - Java WASM interpreter

### Currently implemented
- Stack push/pop works for i32, i64, f32 and f64
- Uses Java 21 pattern matching and records to implement instructions
- Arithmetic and comparison ops implemented
- Basic load/store working
- Unary ops like popcnt, ceil, floor, trunc implemented

### TODO
- Load/store with alignment and offset support
- Functions
- Blocks, loops and conditionals
- Boolean ops

## walrus - Rust WASM interpreter

### TODO 
- Write code!
- Use enums and pattern matching

## Tools:
- [`wasm-tools`](https://github.com/bytecodealliance/wasm-tools)
- 