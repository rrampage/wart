# A simple WASM interpreter in multiple languages

Inspired by [David Beazley's mindblowing talk](https://www.youtube.com/watch?v=r-A78RgMhZU) on live-coding [a WASM interpreter](https://gist.github.com/dabeaz/7d8838b54dba5006c58a40fc28da9d5a)


WASM opcode references:
- [Opcode table](https://pengowray.github.io/wasm-ops/)
- [MDN WASM Instruction reference](https://developer.mozilla.org/en-US/docs/WebAssembly/Reference)

## wasp - Java WASM interpreter

### Currently implemented
#### Machine
- Stack push/pop works for i32, i64, f32 and f64
- Uses Java 21 pattern matching and records to implement instructions
- Arithmetic and comparison ops implemented
- Load/store with alignment and offset support
- Unary ops like popcnt, ceil, floor, trunc implemented
- Functions
  - call and call_indirect
  - Labels set/reset during/after function call
- Bitshift ops
- Blocks, loops and conditionals
- Start

#### WAT Parser
- Instructions
- Imports
- Types
- Functions
- Block comments

#### WASM parser
- Magic bytes and version check
- Implement [LEB128](https://en.wikipedia.org/wiki/LEB128) decode
- Read sections
  - Type 
  - Import
  - Export
  - Start
  - Function
  - Table
  - Memory

#### Interop
- Import Java functions in WASM using MethodHandle for typesafe invoking

### TODO
#### Machine
- Exports

#### WAT Parser
- Exports
- Memory
- Tables
- Start

#### WASM parser
- Implement [LEB128](https://en.wikipedia.org/wiki/LEB128) encode
- Read section metadata
- Read sections
  - Custom
  - Global
  - Element
  - Code
  - Data
  - Data count

#### Interop
- Use exported functions from Java
- Create support infrastructure to pass multiple imports
  - We do not have to pass imports while parsing. We can create stubbed-out functions using the type signature
  - Later, when instantiating machine, we can replace these stubbed functions with method handles from a HashMap<String, MethodHandle>

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

## walrus - Rust WASM interpreter

### TODO 
- Write code!
- Use enums and pattern matching

## Tools:
### [`wasm-tools`](https://github.com/bytecodealliance/wasm-tools)
- Convert `wasm` to `wat` : `wasm-tools print my_file.wasm > my_file.wat`
- Generate valid WASM files: `head -c 1000 /dev/urandom | wasm-tools smith -o test.wasm`
- Generate valid WAT files: `head -c 1000 /dev/urandom | wasm-tools smith | wasm-tools print > test.wat`

### [Web Assembly Binary Toolkit (`wabt`)](https://github.com/WebAssembly/wabt)
- Convert `wat` to `wasm` : `wat2wasm my_file.wat` will create `my_file.wasm`
- Analyze wasm file: `wasm-objdump -x my_file.wasm`. 
  - `-x` shows section details
  - `-d` shows disassembly of all function bodies
  - `-s` shows section contents

### Standard coreutils / binutils
- Read wasm file as hex:
  - `xxd -u -g 1 my_file.wasm` or `hexdump -C my_file.wasm`