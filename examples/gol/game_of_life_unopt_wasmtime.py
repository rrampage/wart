from wasmtime import Module, Linker, Store, Instance
from wasmtime import Func, Table, Global, Memory
import random
import timeit
store = Store()
linker = Linker(store.engine)
filename = "game_of_life_unopt.wasm"
wasm_module = Module.from_file(store.engine, filename)
instance = Instance(store, wasm_module, [])
init_board = instance.exports(store)["initializeBoard"]
set_val_at_pos = instance.exports(store)["setValueAtPosition"]
tick = instance.exports(store)["tick"]
width = 800
height = 600
prob = 0.4
num_iters = 100
print("Instantiation complete")

init_board(store, 800, 600)
for i in range(width):
    for j in range(height):
        r = random.random()
        filled = 1 if r < prob else 0
        set_val_at_pos(store, i, j, filled)
print("INIT DONE")
def tick_call():
    tick(store)

t = timeit.Timer(tick_call)
res = t.timeit(num_iters)
print(res/num_iters)