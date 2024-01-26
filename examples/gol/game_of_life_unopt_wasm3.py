import wasm3
import random
import timeit
wasm_file = "game_of_life_unopt.wasm"
print(wasm_file)
env = wasm3.Environment()
rt = env.new_runtime(2048)
with open(wasm_file, "rb") as f:
    mod = env.parse_module(f.read())
    rt.load(mod)
width = 800
height = 600
prob = 0.4
num_iters = 40
init_board = rt.find_function("initializeBoard")
set_val_at_pos = rt.find_function("setValueAtPosition")
tick = rt.find_function("tick")

# Setup board
init_board(800, 600)
for i in range(width):
    for j in range(height):
        r = random.random()
        filled = 1 if r < prob else 0
        set_val_at_pos(i, j, filled)
print("INIT DONE")
t = timeit.Timer(tick)
# res = t.repeat(10, 10)
res = t.timeit(num_iters)
print(res/num_iters)