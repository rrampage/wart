package rrampage.wasp.data;

public final class WastTrap extends WastCommand {
    private WastAction action;
    private WastValue[] expected;
    private String text;
    public WastTrap() {}
    public WastAction getAction() {return action;}
    public WastValue[] getExpected() {return expected;}
    public void setExpected(WastValue[] expected) { this.expected = expected;}
    public void setAction(WastAction action) {this.action = action;}
    public String getText() {
        return text;
    }
    public void setText(String text) { this.text = text;}
}
