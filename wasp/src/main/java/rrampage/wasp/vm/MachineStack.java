package rrampage.wasp.vm;

import java.util.Arrays;

public class MachineStack {
    long[] array = new long[4096];
    int stackPointer = 0;

    public void push(long value) {
        int l = array.length;
        if (l <= stackPointer) {
            array = Arrays.copyOf(array, (int) (l * 1.5));
        }
        array[stackPointer++] = value;
    }

    public long pop() {
        if (stackPointer <= 0) {
            throw new RuntimeException("STACK_UNDERFLOW");
        }
        return array[--stackPointer];
    }

    public boolean isEmpty() { return stackPointer == 0;}
    public String inspect() {
        if (stackPointer <= 0) { return "";}
        StringBuilder sb = new StringBuilder(stackPointer*10);
        for (int i = stackPointer-1; i > 0; i--) {
            sb.append(array[i]).append(",");
        }
        sb.append(array[0]);
        return sb.toString();
    }
}
