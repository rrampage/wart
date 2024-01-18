package rrampage.wasp.vm;

import rrampage.wasp.data.Function;
import rrampage.wasp.instructions.Instruction;

import java.util.HashMap;
import java.util.SequencedCollection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MachineVisitors {
    public static final MachineVisitor NULL_VISITOR = MachineVisitor.VisitorBuilder.of().build();

    public static final class InstructionCounter {
        private final HashMap<String, Integer> instructionCounter = new HashMap<>();
        public final Consumer<Instruction> preInstructionConsumer = (ins) -> instructionCounter.put(ins.opCode(), instructionCounter.getOrDefault(ins.opCode(), 0)+1);
        public final Consumer<Machine> end = (m) -> System.out.println(instructionCounter);
    }

    public static MachineVisitor instructionCountVisitor() {
        var ic = new InstructionCounter();
        return MachineVisitor.VisitorBuilder.of()
                .preInstruction(ic.preInstructionConsumer).end(ic.end).build();
    }

    public static MachineVisitor logVisitor() {
        var ic = new InstructionCounter();
        return MachineVisitor.VisitorBuilder.of()
                .stack(stackOpLogger, stackViewLogger)
                .preInstruction(ic.preInstructionConsumer, insStartLogger)
                .preFunction(functionStartLogger).postFunction(functionEndLogger)
                .end(ic.end).build();
    }

    public static MachineVisitor debugVisitor() {
        var d = new Debugger();
        return Debugger.getMachineVisitor();
    }

    public static final Consumer<Function> functionStartLogger = (fun) -> System.out.println("FUNCTION_START: " + fun.name() + " " + fun.type());
    public static final Consumer<Function> functionEndLogger = (fun) -> System.out.println("FUNCTION_END: " + fun.name() + " " + fun.type());
    public static final Consumer<Instruction> insStartLogger = (ins) -> System.out.println("INSTRUCTION_START: " + ins.opCode());
    public static final Consumer<Instruction> insEndLogger = (ins) -> System.out.println("INSTRUCTION_END: " + ins.opCode());
    public static final BiConsumer<StackOp, SequencedCollection<Long>> stackOpLogger = (stackOp, stack) -> System.out.println("STACK_OP: " + stackOp);
    public static final BiConsumer<StackOp, SequencedCollection<Long>> stackViewLogger = (stackOp, stack) -> System.out.println("STACK_VIEW: " + stack);
}
