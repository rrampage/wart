# A simple WASM interpreter

```bash
go mod tidy
go test -v
```

Currently implemented:
- Works with i32
- Support for load/store
- Support for arithmetic
- Support for simple functions

TODO:
- Branching
- Blocks
- Support i64, f32, f64
- Imports
- Run on simple wasm files
