package rrampage.wasp.vm;

import rrampage.wasp.data.Function;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.instructions.NullaryInstruction;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.function.Consumer;

public class MachineVisitors {
    public static final MachineVisitor NULL_VISITOR = MachineVisitor.VisitorBuilder.of().build();

    public static final class InstructionCounter {
        private final HashMap<String, Integer> instructionCounter = new HashMap<>();
        private final ArrayDeque<String> callStack = new ArrayDeque<>();
        private Instruction currentInstruction = NullaryInstruction.NOP;
        private Instruction lastSuccessfulInstruction = NullaryInstruction.NOP;
        private Machine machine;
        public void start(Machine m) {
            this.machine = m;
        }
        public void preInstructionConsumer(Instruction ins) {
            currentInstruction = ins;
            instructionCounter.merge(ins.opCode(), 1, (v1, _v2) -> v1+1);
        }
        public void postInstructionConsumer( Instruction ins) {
            lastSuccessfulInstruction = ins;
        }
        public void preFunction(Function fun) {
            System.out.println("FUNCTION_START: " + fun.name() + " " + fun.type());
            System.out.println(this.callStack);
            this.callStack.push(fun.name());
        }
        public void postFunction(Function fun) {
            System.out.println(STR."FUNCTION_END: \{fun.name()} \{fun.type()}");
            this.callStack.pop();
            System.out.println(STR."FUNCTION_END_CALL_STACK: \{this.callStack}");
        }
        public void end(Machine m) {
            System.out.println("Last successful instruction: " + lastSuccessfulInstruction.opCode());
            System.out.println("Last executed instruction: " + currentInstruction.opCode());
            System.out.println("Stack: " + m.stackView());
            System.out.println(instructionCounter);
        };
    }

    public static MachineVisitor logVisitor() {
        var ic = new InstructionCounter();
        return MachineVisitor.VisitorBuilder.of()
                .start(ic::start)
                .preInstruction(ic::preInstructionConsumer)
                .postInstruction(ic::postInstructionConsumer)
                .preFunction(ic::preFunction).postFunction(ic::postFunction)
                .end(ic::end).build();
    }

    public static MachineVisitor debugVisitor() {
        return Debugger.getMachineVisitor();
    }

    public static final Consumer<Function> functionStartLogger = (fun) -> System.out.println("FUNCTION_START: " + fun.name() + " " + fun.type());
    public static final Consumer<Function> functionEndLogger = (fun) -> System.out.println("FUNCTION_END: " + fun.name() + " " + fun.type());
    public static final Consumer<Instruction> insStartLogger = (ins) -> System.out.println("INSTRUCTION_START: " + ins.opCode());
    public static final Consumer<Instruction> insEndLogger = (ins) -> System.out.println("INSTRUCTION_END: " + ins.opCode());
}
