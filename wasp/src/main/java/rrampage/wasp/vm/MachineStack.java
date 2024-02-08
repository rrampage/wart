package rrampage.wasp.vm;

import java.util.Arrays;

public class MachineStack {
    private long[] array = new long[4096];
    private int stackPointer = 0;

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

    public int stackPointer() {return stackPointer;}
    public void dropKeep(int drop, int keep) {
        // If there is no value to be dropped, return
        if (drop == 0) {
            return;
        }
        if (keep < 0 || drop < 0 || keep + drop > stackPointer) {
            throw new RuntimeException(STR."STACK_DROP_KEEP_ERROR sp: \{stackPointer} drop: \{drop} keep: \{keep}");
        }
        // keep the 1st "keep" values
        // drop "drop" values below
        int sp = stackPointer;
        long[] keeps = new long[keep]; // copy to temp array
        System.arraycopy(array, sp-keep, keeps, 0, keep);
        System.arraycopy(keeps, 0, array, sp-drop-keep, keep);
        stackPointer = sp - drop;
    }

    public static void main(String[] args) {
        MachineStack ms = new MachineStack();
        ms.push(1);
        ms.push(11);
        ms.push(111);
        ms.dropKeep(1, 1);
        System.out.println(ms.inspect());
    }
}
