package rrampage.wasp.vm;

import rrampage.wasp.data.Function;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.instructions.NullaryInstruction;

import java.util.HashMap;
import java.util.function.Consumer;

public class MachineVisitors {
    public static final MachineVisitor NULL_VISITOR = MachineVisitor.VisitorBuilder.of().build();

    public static final class InstructionCounter {
        private final HashMap<String, Integer> instructionCounter = new HashMap<>();
        private Instruction currentInstruction = NullaryInstruction.NOP;
        private Instruction lastSuccessfulInstruction = NullaryInstruction.NOP;
        public final Consumer<Instruction> preInstructionConsumer = (ins) -> {
            currentInstruction = ins;
            instructionCounter.merge(ins.opCode(), 1, (v1, _v2) -> v1+1);
            // instructionCounter.put(ins.opCode(), instructionCounter.getOrDefault(ins.opCode(), 0)+1);
        };
        public final Consumer<Instruction> postInstructionConsumer = (ins) -> {
            lastSuccessfulInstruction = ins;
        };
        public final Consumer<Machine> end = (m) -> {
            System.out.println("Last successful instruction: " + lastSuccessfulInstruction.opCode());
            System.out.println("Last executed instruction: " + currentInstruction.opCode());
            System.out.println("Stack: " + m.stackView());
            System.out.println(instructionCounter);
        };
    }

    public static MachineVisitor instructionCountVisitor() {
        var ic = new InstructionCounter();
        return MachineVisitor.VisitorBuilder.of()
                .preInstruction(ic.preInstructionConsumer).postInstruction(ic.postInstructionConsumer).end(ic.end).build();
    }

    public static MachineVisitor logVisitor() {
        var ic = new InstructionCounter();
        return MachineVisitor.VisitorBuilder.of()
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
}
