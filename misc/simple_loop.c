// clang-11 -g -target wasm32 -nostdlib -Os -fcolor-diagnostics -fno-crash-diagnostics -o simple_loop.o -c simple_loop.c
// wasm-ld --no-entry --export-all --lto-O3 --allow-undefined --import-memory simple_loop.o -o simple_loop.wasm
int fun1(int n) {
    if (n < 10) {
        return n * 2;
    }
    if (n > 100) {
        return n * 1600;
    }
    int i = 10, j = 0;
    int res = 0;
    while (i < 100) {
        while (j < n) {
            res += i*j;
            j++;
        }
        i += 2;
    }
    return res;
}