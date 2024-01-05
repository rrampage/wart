# Examples from the official [Web Assembly Test Suite](https://github.com/WebAssembly/testsuite)

This directory contains `.wast` files copied from the official test-suite repo.
Running the command `wast2json my_file.wast` will create associated `.wasm` files and a JSON file of format:

```json
{
  "source_filename": "fac.wast",
  "commands": [
  {"type": "module", "line": 1, "filename": "fac.0.wasm"}, 
  {"type": "assert_return", "line": 102, "action": {"type": "invoke", "field": "fac-rec", "args": [{"type": "i64", "value": "25"}]}, "expected": [{"type": "i64", "value": "7034535277573963776"}]}, 
  {"type": "assert_return", "line": 103, "action": {"type": "invoke", "field": "fac-iter", "args": [{"type": "i64", "value": "25"}]}, "expected": [{"type": "i64", "value": "7034535277573963776"}]}, 
  {"type": "assert_return", "line": 104, "action": {"type": "invoke", "field": "fac-rec-named", "args": [{"type": "i64", "value": "25"}]}, "expected": [{"type": "i64", "value": "7034535277573963776"}]}, 
  {"type": "assert_return", "line": 105, "action": {"type": "invoke", "field": "fac-iter-named", "args": [{"type": "i64", "value": "25"}]}, "expected": [{"type": "i64", "value": "7034535277573963776"}]}, 
  {"type": "assert_return", "line": 106, "action": {"type": "invoke", "field": "fac-opt", "args": [{"type": "i64", "value": "25"}]}, "expected": [{"type": "i64", "value": "7034535277573963776"}]}, 
  {"type": "assert_return", "line": 107, "action": {"type": "invoke", "field": "fac-ssa", "args": [{"type": "i64", "value": "25"}]}, "expected": [{"type": "i64", "value": "7034535277573963776"}]}, 
  {"type": "assert_exhaustion", "line": 109, "action": {"type": "invoke", "field": "fac-rec", "args": [{"type": "i64", "value": "1073741824"}]}, "text": "call stack exhausted", "expected": [{"type": "i64"}]}
 ]
}

```