package rrampage.wasp.vm;

import rrampage.wasp.data.Function;
import rrampage.wasp.instructions.Instruction;

public class MachineVisitor {
    final InstructionConsumer[] preInstructionVisitors;
    final InstructionConsumer[] postInstructionVisitors;
    final FunctionConsumer[] preFunctionVisitors;
    final FunctionConsumer[] postFunctionVisitors;
    final Runnable[] startVisitors;
    final Runnable[] endVisitors;
    final boolean isPreInstruction;
    final boolean isPostInstruction;
    final boolean isPreFunction;
    final boolean isPostFunction;
    final boolean isStart;
    final boolean isEnd;

    private MachineVisitor(VisitorBuilder builder) {
        this.preInstructionVisitors = builder.preInstructionVisitors;
        this.postInstructionVisitors = builder.postInstructionVisitors;
        this.preFunctionVisitors = builder.preFunctionVisitors;
        this.postFunctionVisitors = builder.postFunctionVisitors;
        this.startVisitors = builder.startVisitors;
        this.endVisitors = builder.endVisitors;
        this.isPreInstruction = this.preInstructionVisitors != null && this.preInstructionVisitors.length > 0;
        this.isPostInstruction = this.postInstructionVisitors != null && this.postInstructionVisitors.length > 0;
        this.isPreFunction = this.preFunctionVisitors != null && this.preFunctionVisitors.length > 0;
        this.isPostFunction = this.postFunctionVisitors != null && this.postFunctionVisitors.length > 0;
        this.isStart = this.startVisitors != null && this.startVisitors.length > 0;
        this.isEnd = this.endVisitors != null && this.endVisitors.length > 0;
    }

    public void visitPreFunction(Function f) {
        if (!isPreFunction) {return;}
        for (var c : preFunctionVisitors) { c.accept(f);}
    }

    public void visitPostFunction(Function f) {
        if (!isPreFunction) {return;}
        for (var c : preFunctionVisitors) { c.accept(f);}
    }

    public void visitPreInstruction(Instruction i) {
        if (!isPreInstruction) {return;}
        for (var c : preInstructionVisitors) { c.accept(i);}
    }

    public void visitPostInstruction(Instruction i) {
        if (!isPostInstruction) {return;}
        for (var c : postInstructionVisitors) { c.accept(i);}
    }

    public void start() {
        if (!isStart) {return;}
        for (var r : startVisitors) { r.run();}
    }

    public void end() {
        if (!isEnd) {return;}
        for (var r : endVisitors) { r.run();}
    }

    public static class VisitorBuilder {
        InstructionConsumer[] preInstructionVisitors;
        InstructionConsumer[] postInstructionVisitors;
        FunctionConsumer[] preFunctionVisitors;
        FunctionConsumer[] postFunctionVisitors;
        Runnable[] startVisitors;
        Runnable[] endVisitors;

        private VisitorBuilder() {}

        public static VisitorBuilder of() {
            return new VisitorBuilder();
        }

        public final VisitorBuilder preInstruction(InstructionConsumer... visitors) {
            this.preInstructionVisitors = visitors;
            return this;
        }

        public final VisitorBuilder postInstruction(InstructionConsumer... visitors) {
            this.postInstructionVisitors = visitors;
            return this;
        }

        public final VisitorBuilder preFunction(FunctionConsumer... visitors) {
            this.preFunctionVisitors = visitors;
            return this;
        }

        public final VisitorBuilder postFunction(FunctionConsumer... visitors) {
            this.postFunctionVisitors = visitors;
            return this;
        }

        public final VisitorBuilder start(Runnable... visitors) {
            this.startVisitors = visitors;
            return this;
        }

        public final VisitorBuilder end(Runnable... visitors) {
            this.endVisitors = visitors;
            return this;
        }

        public MachineVisitor build() {
            return new MachineVisitor(this);
        }
    }

    public interface InstructionConsumer {
        void accept(final Instruction instruction);
    }

    public interface FunctionConsumer {
        void accept(final Function function);
    }
}
