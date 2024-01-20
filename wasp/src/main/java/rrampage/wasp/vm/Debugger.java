package rrampage.wasp.vm;

import rrampage.wasp.data.Function;
import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.ValueType;
import rrampage.wasp.data.Variable;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.instructions.GlobalInstruction;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.instructions.StoreInstruction;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.ImportUtils;

import java.nio.file.Paths;
import java.util.*;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class Debugger {
    /*
        Building this as a Visitor
        TODO:
        1. Start the machine in a paused state -> DONE
        2. Step through instructions one at a time (s) -> DONE
        3. Run till the next breakpoint (r) -> WIP
        4. Set breakpoint (b) -> DONE
            b fun_idx ins_idx
            Function index, instruction index -> Sort??
            function f -> function g -> function h
        5. Watch variables/memory (w)
            w g idx -> DONE
            w m addr type
            WatchGlobal(index)
            WatchMemory(addr, offset)
        6. Print function name, function code, stack and variable values (p) -> Local variables LATER
        7. Invoke an exported function (i) -> DONE
        8. Remove breakpoints
        Read input from stdin (Scanner ??? )
        How to set break/watch points for complex instructions like loop/block/if/else
     */
    private static final String helpCommands = """
            s:  Step through next instruction
            b:  Set breakpoint at function index and instruction index.
                    e.g `b 0 3` will set a breakpoint at function 0 and instruction number 3.
                    Use `pf` to see correct instruction index for function.
            w:  Set watch point on a variable or memory address.
                    e.g `w g 0` will watch the global variable at index 0 for changes and stop execution if it changes
            r:  Run till next breakpoint/watch point
            pc: Print callStack
            ps: Print machine stack
            pf: Print function
            pv: Print variable
            ?:  Print (this) help message
            """;
    private final Scanner scanner = new Scanner(System.in);
    private boolean isRunning = false;
    private final ArrayDeque<String> callStack = new ArrayDeque<>();
    private Machine machine;
    private Function function;
    private final ArrayDeque<Integer> instructionPointers = new ArrayDeque<>();
    private final HashSet<BreakPoint> breakPoints = new HashSet<>();
    private final HashMap<Integer, WatchGlobal> globalWatchPoints = new HashMap<>();

    public Debugger() {}

    private void start(Machine machine) {
        this.machine = machine;
        outer:
        while (true) {
            System.out.println("Available functions:" + machine.exports().keySet());
            System.out.println("Enter function to invoke with space separated arguments:");
            String inp = scanner.nextLine();
            System.out.printf("INPUT: %s\n", inp);
            var tokens = inp.split(" ");
            var funName = tokens[0];
            if (!machine.exports().containsKey(funName) || !(machine.exports().get(funName) instanceof Function f)) {
                return;
            }
            // Check len token == 1 + numParams
            if (tokens.length != 1 + f.numParams()) {
                System.out.println("Invalid number of arguments passed for " + funName + ". Expecting " + f.numParams());
                continue;
            }
            var types = f.type().paramTypes();
            ConstInstruction[] expr = new ConstInstruction[f.numParams()];
            for (var i = 1; i <= f.numParams(); i++) {
                String arg = tokens[i];
                // TODO type check and parse to correct type
                var type = types[i-1];
                if (Objects.requireNonNull(type) instanceof ValueType.NumType n) {
                    expr[i-1] = switch (n) {
                        case ValueType.NumType.I32 -> constOf(Integer.parseInt(arg));
                        case ValueType.NumType.I64 -> constOf(Long.parseLong(arg));
                        case ValueType.NumType.F32 -> constOf(Float.parseFloat(arg));
                        case ValueType.NumType.F64 -> constOf(Double.parseDouble(arg));
                    };
                } else {
                    System.out.println("Could not parse argument " + arg);
                    continue outer;
                }
            }
            System.out.println(helpCommands);
            machine.invoke(funName, expr);
            this.callStack.push(funName);
            return;
        }
    }

    private void end(Machine machine) {
        System.out.println(machine.inspectStack());
    }

    private void preFunction(Function f) {
        this.instructionPointers.push(0);
        this.function = f;
        System.out.println(this.callStack);
        this.callStack.add(f.name());
    }

    private void postFunction(Function f) {
        this.instructionPointers.pop();
        this.function = null;
        var funName = this.callStack.pop();
        System.out.println("Finished executing function: " + funName);
    }

    private void preInstruction(Instruction ins) {
        int instructionIndex = instructionPointers.isEmpty() ? -1 : instructionPointers.peek();
        int functionIndex = callStack.isEmpty() ? -1 : getFunctionIndex(callStack.peek());
        var bp = new BreakPoint(functionIndex, instructionIndex);
        var isBreakPoint = breakPoints.contains(bp);
        /*System.out.printf("Ins: %s IsBreakPoint: %b IsRunning %b Curr: %s BreakPoints: %s InsPtrs: %s\n",
                ins.opCode(), isBreakPoint, isRunning, bp, breakPoints, instructionPointers);*/

        if (isRunning && !isBreakPoint) {
            return;
        }
        // TODO
        /*
            If ins instance of loop/block/if/if-else
            push 0 to instructionPointer

            in postIns If ins instance of loop/block/if/if-else
            pop from instructionPointer

            for if-else, how do we keep track of ins -> if i check stack for value of 1
         */
        isRunning = false;
        while (true) {
            System.out.println("Enter next command:");
            String inp = scanner.nextLine();
            System.out.printf("INPUT: %s Ins: %s\n", inp, ins.opCode());
            var tokens = inp.split(" ");
            var command = tokens[0];
            switch (command) {
                case "s" -> {
                    return;
                }
                case "pc" -> System.out.println("Call Stack: " + this.callStack);
                case "ps" -> System.out.println("Machine Stack: " + this.machine.inspectStack());
                case "pf" -> {
                    if (this.function == null) {
                        System.out.println("Not in a function....");
                        continue;
                    }
                    System.out.printf("Function: %d Params %d\n", this.getFunctionIndex(this.function.name()), this.function.numParams());
                    for (int  i = 0; i < this.function.code().length; i++) {
                        String isCurrentInstruction =
                                (this.instructionPointers.isEmpty() || i != this.instructionPointers.peek()) ? "" : "*";
                        System.out.printf("\t%d: %s%s\n", i, isCurrentInstruction, this.function.code()[i]);
                    }
                }
                case "pv" -> {
                    // var l = function.locals();
                    // TODO: Create and Expose Stack frame for locals
                    var g = machine.globals();
                    System.out.println("Global variables:");
                    for (int  i = 0; i < g.length; i++) {
                        System.out.printf("\t%d: %s\n", i, Variable.debug(g[i]));
                    }
                }
                case "r" -> {
                    isRunning = true;
                    return;
                }
                case "b" -> {
                    // Check 3 args in token
                    if (tokens.length != 1 && tokens.length != 3) {
                        System.out.printf("Invalid breakpoint %s\n", inp);
                        continue;
                    }
                    var funcIdx = (tokens.length == 1) ? functionIndex : Integer.parseInt(tokens[1]);
                    var instIdx = (tokens.length == 1) ? instructionIndex :Integer.parseInt(tokens[2]);
                    var status = createBreakPoint(funcIdx, instIdx);
                    if (status) {
                        System.out.println("Breakpoint created!");
                    } else {
                        System.out.println("Failed to create breakpoint");
                    }
                }
                case "w" -> {
                    if (tokens.length == 1 || tokens.length > 4) {
                        System.out.printf("Invalid watch %s\n", inp);
                        continue;
                    }
                    var watchType = tokens[1];
                    if (!watchType.equals("g")) {
                        System.out.printf("Invalid watch %s\n", inp);
                        continue;
                    }
                    var watchIdx = Integer.parseInt(tokens[2]);
                    if (watchIdx < 0 || watchIdx >= this.machine.globals().length) {
                        System.out.printf("Invalid watch %s\n", inp);
                        continue;
                    }
                    globalWatchPoints.put(watchIdx, new WatchGlobal(machine.globals()[watchIdx]));
                }
                case "?" -> System.out.println(helpCommands);
                default -> {}
            }
        }
    }

    private void postInstruction(Instruction ins) {
        if (this.instructionPointers.isEmpty()) {
            return;
        }
        this.instructionPointers.push(this.instructionPointers.pop()+1);
        if ((ins instanceof GlobalInstruction.GlobalSet gs) &&
                        globalWatchPoints.containsKey(gs.val())
        ) {
            var wg = globalWatchPoints.get(gs.val());
            var isChanged = wg.value != wg.variable.getValAsLong();
            if (isChanged) {
                System.out.printf("Watch point triggered for global %d Old: %d New: %d\n", gs.val(), wg.value, wg.variable.getValAsLong());
                isRunning = false;
                wg.value = wg.variable.getValAsLong();
            }
            // TODO: Can be used to set a message
        }
        if (ins instanceof StoreInstruction s) {

        }
    }

    private int getFunctionIndex(String funName) {
        var tokens = funName.split("Function_");
        if (tokens.length != 2) {
            throw new RuntimeException("Function index parsed incorrectly");
        }
        return Integer.parseInt(tokens[1]);
    }

    public static MachineVisitor getMachineVisitor() {
        var d = new Debugger();
        return MachineVisitor.VisitorBuilder.of()
                .start(d::start)
                .preFunction(d::preFunction)
                .preInstruction(d::preInstruction)
                .postInstruction(d::postInstruction)
                .postFunction(d::postFunction)
                .end(d::end)
                .build();
    }

    private boolean createBreakPoint(int funcIdx, int instIdx) {
        if (funcIdx < 0 || instIdx < 0 || funcIdx >= this.machine.functions().length) {
            return false;
        }
        // Code length
        Function f = this.machine.functions()[funcIdx];
        if (instIdx >= f.code().length) {
            return false;
        }
        this.breakPoints.add(new BreakPoint(funcIdx, instIdx));
        return true;
    }

    public static void main(String[] args) throws Exception {
        String fileName = "./examples/testsuite/fac.0.wasm";
        String path = Paths.get(fileName).toAbsolutePath().normalize().toString();
        var parser = WasmParser.fromFile(path);
        var module = parser.parseModule();
        var machine = module.instantiate(
                Map.of("host", Map.of("print", ImportUtils.generateLoggerHandle(new FunctionType(new ValueType.NumType[]{ValueType.NumType.I32}, new ValueType.NumType[]{ValueType.NumType.I32})))),
                Debugger.getMachineVisitor());
        // machine.invoke("fac-iter", constOf(21L));
    }

    private static class BreakPoint{
        int funcIndex;
        int[] instructionIndex;
        public BreakPoint(int funcIndex, int... instructionIndex) {
            this.funcIndex = funcIndex;
            this.instructionIndex = instructionIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BreakPoint that = (BreakPoint) o;
            return funcIndex == that.funcIndex && Arrays.equals(instructionIndex, that.instructionIndex);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(funcIndex);
            result = 31 * result + Arrays.hashCode(instructionIndex);
            return result;
        }

        @Override
        public String toString() {
            return "BreakPoint{" +
                    "funcIndex=" + funcIndex +
                    ", instructionIndex=" + Arrays.toString(instructionIndex) +
                    '}';
        }
    }

    private static class WatchGlobal {
        public final Variable variable;
        public long value;
        public WatchGlobal(Variable variable) {
            this.variable = variable;
            this.value = variable.getValAsLong();
        }
    }
}
