package rrampage.wasp.vm;

import java.util.HashMap;

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
}
