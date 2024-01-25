package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstInstruction;

import java.util.Arrays;

public final class WastReturn extends WastCommand {
    private WastAction action;
    private WastValue[] expected;
    public WastReturn() {}
    public WastAction getAction() {return action;}
    public WastValue[] getExpected() {return expected;}
    public void setExpected(WastValue[] expected) { this.expected = expected;}
    public void setAction(WastAction action) {this.action = action;}

    public AssertReturn toAssert() {
        ConstInstruction[] args = Arrays.stream(action.args()).map(WastValue::toConst).toArray(ConstInstruction[]::new);
        ConstInstruction[] expected = Arrays.stream(getExpected()).map(WastValue::toConst).toArray(ConstInstruction[]::new);
        return new AssertReturn(action.field(), args, expected);
    }
}
