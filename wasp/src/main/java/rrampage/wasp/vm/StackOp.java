package rrampage.wasp.vm;

public sealed interface StackOp {
    record PushInt(int data) implements StackOp{ public String toString() {return "PUSH_INT " + data;} }
    record PushLong(long data) implements StackOp{ public String toString() {return "PUSH_LONG " + data;}}
    record PushFloat(float data) implements StackOp{ public String toString() {return "PUSH_FLOAT " + data;}}
    record PushDouble(double data) implements StackOp{public String toString() {return "PUSH_DOUBLE " + data;}}
    enum Pop implements StackOp {
        POP_INT,
        POP_LONG,
        POP_FLOAT,
        POP_DOUBLE
    }
}
