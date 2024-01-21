package rrampage.wasp.vm;

import rrampage.wasp.data.Function;
import rrampage.wasp.data.Variable;
import rrampage.wasp.instructions.Instruction;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MachineVisitor {
    final Consumer<Instruction> preInstructionVisitor;
    final Consumer<Instruction> postInstructionVisitor;
    final Consumer<Function> preFunctionVisitor;
    final Consumer<Function> postFunctionVisitor;
    final Consumer<Machine> startVisitor;
    final Consumer<Machine> endVisitor;
    final boolean hasPreInstructionVisitor;
    final boolean hasPostInstructionVisitor;
    final boolean hasPreFunctionVisitor;
    final boolean hasPostFunctionVisitor;
    final boolean hasStartVisitor;
    final boolean hasEndVisitor;

    private MachineVisitor(VisitorBuilder builder) {
        this.preInstructionVisitor = builder.preInstructionVisitor;
        this.postInstructionVisitor = builder.postInstructionVisitor;
        this.preFunctionVisitor = builder.preFunctionVisitor;
        this.postFunctionVisitor = builder.postFunctionVisitor;
        this.startVisitor = builder.startVisitor;
        this.endVisitor = builder.endVisitor;
        this.hasPreInstructionVisitor = this.preInstructionVisitor != null;
        this.hasPostInstructionVisitor = this.postInstructionVisitor != null;
        this.hasPreFunctionVisitor = this.preFunctionVisitor != null;
        this.hasPostFunctionVisitor = this.postFunctionVisitor != null;
        this.hasStartVisitor = this.startVisitor != null;
        this.hasEndVisitor = this.endVisitor != null;
    }

    public void visitPreFunction(Function f) {
        if (!hasPreFunctionVisitor) {return;}
        preFunctionVisitor.accept(f);
    }

    public void visitPostFunction(Function f) {
        if (!hasPostFunctionVisitor) {return;}
        postFunctionVisitor.accept(f);
    }

    public void visitPreInstruction(Instruction i) {
        if (!hasPreInstructionVisitor) {return;}
        preInstructionVisitor.accept(i);
    }

    public void visitPostInstruction(Instruction i) {
        if (!hasPostInstructionVisitor) {return;}
        postInstructionVisitor.accept(i);
    }

    public void start(Machine m) {
        System.out.println("START_CALLED");
        if (!hasStartVisitor) {return;}
        startVisitor.accept(m);
    }

    public void end(Machine m) {
        if (!hasEndVisitor) {return;}
        endVisitor.accept(m);
    }

    public static class VisitorBuilder {
        Consumer<Instruction> preInstructionVisitor;
        Consumer<Instruction> postInstructionVisitor;
        Consumer<Function> preFunctionVisitor;
        Consumer<Function> postFunctionVisitor;
        Consumer<Machine> startVisitor;
        Consumer<Machine> endVisitor;
        Consumer<Variable> globalVisitor;
        Consumer<Variable> localVisitor;

        private VisitorBuilder() {}

        public static VisitorBuilder of() {
            return new VisitorBuilder();
        }

        @SafeVarargs
        public final VisitorBuilder preInstruction(Consumer<Instruction>... visitors) {
            this.preInstructionVisitor = compose(visitors);
            return this;
        }

        @SafeVarargs
        public final VisitorBuilder postInstruction(Consumer<Instruction>... visitors) {
            this.postInstructionVisitor = compose(visitors);
            return this;
        }

        @SafeVarargs
        public final VisitorBuilder preFunction(Consumer<Function>... visitors) {
            this.preFunctionVisitor = compose(visitors);
            return this;
        }

        @SafeVarargs
        public final VisitorBuilder postFunction(Consumer<Function>... visitors) {
            this.postFunctionVisitor = compose(visitors);
            return this;
        }

        @SafeVarargs
        public final VisitorBuilder start(Consumer<Machine>... visitors) {
            this.startVisitor = compose(visitors);
            return this;
        }

        @SafeVarargs
        public final VisitorBuilder end(Consumer<Machine>... visitors) {
            this.endVisitor = compose(visitors);
            return this;
        }

        @SafeVarargs
        public final VisitorBuilder global(final Consumer<Variable>... visitors) {
            this.globalVisitor = compose(visitors);
            return this;
        }


        @SafeVarargs
        public final VisitorBuilder local(final Consumer<Variable>... visitors) {
            this.localVisitor = compose(visitors);
            return this;
        }

        public final MachineVisitor build() {
            return new MachineVisitor(this);
        }

        @SafeVarargs
        private static <T> Consumer<T> compose(final Consumer<T>... visitors) {
            if (visitors == null || visitors.length == 0) { return null; }
            return (t) -> {
                for (var c : visitors) {
                    Objects.requireNonNull(c);
                    c.accept(t);
                }
            };
        }

        @SafeVarargs
        private static <T, U> BiConsumer<T, U> compose(final BiConsumer<T, U>... visitors) {
            if (visitors == null || visitors.length == 0) { return null; }
            return (t, u) -> {
                for (var c : visitors) {
                    Objects.requireNonNull(c);
                    c.accept(t, u);
                }
            };
        }

        private static Runnable compose(final Runnable... visitors) {
            if (visitors == null || visitors.length == 0) { return null; }
            return () -> {
                for (var r : visitors) {
                    Objects.requireNonNull(r);
                    r.run();
                }
            };
        }
    }
}
