.PHONY: clean wasm wat
clean:
	rm simple_loop.o simple_loop.wasm
CC = clang-11
LD = wasm-ld-11
c_flags := -Wall -Wextra -std=c99 -g -target wasm32  -nostdlib -Os -fcolor-diagnostics -fno-crash-diagnostics
ld_flags := --no-entry --export-all --lto-O3 --allow-undefined --import-memory
simple_loop.o: simple_loop.c
	$(CC) $(c_flags) -o simple_loop.o -c simple_loop.c
wasm: simple_loop.c simple_loop.o
	$(LD) $(ld_flags) -o simple_loop.wasm simple_loop.o
wat: wasm
	wasm2wat simple_loop.wasm -o simple_loop.wat
