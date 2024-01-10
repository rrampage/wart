package rrampage.wasp.vm;

import java.util.HashMap;
import static rrampage.wasp.vm.MachineVisitor.*;

public class MachineVisitors {
    public static final MachineVisitor NULL_VISITOR = MachineVisitor.VisitorBuilder.of().build();

    public static final class InstructionCounter {
        private final HashMap<String, Integer> instructionCounter = new HashMap<>();
        public final MachineVisitor.InstructionConsumer preInstructionConsumer = (ins) -> instructionCounter.put(ins.opCode(), instructionCounter.getOrDefault(ins.opCode(), 0)+1);
        public final Runnable end = () -> System.out.println(instructionCounter);
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

    public static final FunctionConsumer functionStartLogger = (fun) -> System.out.println("FUNCTION_START: " + fun.name() + " " + fun.type());
    public static final FunctionConsumer functionEndLogger = (fun) -> System.out.println("FUNCTION_END: " + fun.name() + " " + fun.type());
    public static final InstructionConsumer insStartLogger = (ins) -> System.out.println("INSTRUCTION_START: " + ins.opCode());
    public static final InstructionConsumer insEndLogger = (ins) -> System.out.println("INSTRUCTION_END: " + ins.opCode());
    public static final StackConsumer stackOpLogger = (stackOp, stack) -> System.out.println("STACK_OP: " + stackOp);
    public static final StackConsumer stackViewLogger = (stackOp, stack) -> System.out.println("STACK_VIEW: " + stack);
}
