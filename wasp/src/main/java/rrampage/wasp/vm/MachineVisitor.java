package rrampage.wasp.vm;

import rrampage.wasp.data.Function;
import rrampage.wasp.instructions.Instruction;

import java.util.SequencedCollection;

public class MachineVisitor {
    final InstructionConsumer[] preInstructionVisitors;
    final InstructionConsumer[] postInstructionVisitors;
    final FunctionConsumer[] preFunctionVisitors;
    final FunctionConsumer[] postFunctionVisitors;
    final Runnable[] startVisitors;
    final Runnable[] endVisitors;
    final StackConsumer[] stackVisitors;
    final boolean hasPreInstructionVisitor;
    final boolean hasPostInstructionVisitor;
    final boolean hasPreFunctionVisitor;
    final boolean hasPostFunctionVisitor;
    final boolean hasStartVisitor;
    final boolean hasEndVisitor;
    final boolean hasStackVisitor;

    private MachineVisitor(VisitorBuilder builder) {
        this.preInstructionVisitors = builder.preInstructionVisitors;
        this.postInstructionVisitors = builder.postInstructionVisitors;
        this.preFunctionVisitors = builder.preFunctionVisitors;
        this.postFunctionVisitors = builder.postFunctionVisitors;
        this.startVisitors = builder.startVisitors;
        this.endVisitors = builder.endVisitors;
        this.stackVisitors = builder.stackVisitors;
        this.hasPreInstructionVisitor = this.preInstructionVisitors != null && this.preInstructionVisitors.length > 0;
        this.hasPostInstructionVisitor = this.postInstructionVisitors != null && this.postInstructionVisitors.length > 0;
        this.hasPreFunctionVisitor = this.preFunctionVisitors != null && this.preFunctionVisitors.length > 0;
        this.hasPostFunctionVisitor = this.postFunctionVisitors != null && this.postFunctionVisitors.length > 0;
        this.hasStartVisitor = this.startVisitors != null && this.startVisitors.length > 0;
        this.hasEndVisitor = this.endVisitors != null && this.endVisitors.length > 0;
        this.hasStackVisitor = this.stackVisitors != null && this.stackVisitors.length > 0;
    }

    public void visitPreFunction(Function f) {
        if (!hasPreFunctionVisitor) {return;}
        for (var c : preFunctionVisitors) { c.accept(f);}
    }

    public void visitPostFunction(Function f) {
        if (!hasPostFunctionVisitor) {return;}
        for (var c : postFunctionVisitors) { c.accept(f);}
    }

    public void visitPreInstruction(Instruction i) {
        if (!hasPreInstructionVisitor) {return;}
        for (var c : preInstructionVisitors) { c.accept(i);}
    }

    public void visitPostInstruction(Instruction i) {
        if (!hasPostInstructionVisitor) {return;}
        for (var c : postInstructionVisitors) { c.accept(i);}
    }

    public void start() {
        if (!hasStartVisitor) {return;}
        for (var r : startVisitors) { r.run();}
    }

    public void end() {
        if (!hasEndVisitor) {return;}
        for (var r : endVisitors) { r.run();}
    }

    public void visitStack(StackOp op, SequencedCollection<Long> stack) {
        if (!hasStackVisitor) {return;}
        for (var r : stackVisitors) { r.accept(op, stack);}
    }

    public static class VisitorBuilder {
        InstructionConsumer[] preInstructionVisitors;
        InstructionConsumer[] postInstructionVisitors;
        FunctionConsumer[] preFunctionVisitors;
        FunctionConsumer[] postFunctionVisitors;
        StackConsumer[] stackVisitors;
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

        public final VisitorBuilder stack(StackConsumer ... visitors) {
            this.stackVisitors = visitors;
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

    public interface StackConsumer {
        void accept(StackOp op, SequencedCollection<Long> stack);
    }
}
