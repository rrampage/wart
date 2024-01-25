package rrampage.wasp.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = WastCommand.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WastReturn.class, name = "assert_return"),
        @JsonSubTypes.Type(value = WastTrap.class, name = "assert_trap"),
        @JsonSubTypes.Type(value = WastModule.class, name = "module"),
        @JsonSubTypes.Type(value = WastInvalid.class, name = "assert_invalid"),
        @JsonSubTypes.Type(value = WastInvalid.class, name = "assert_malformed"),
})
public sealed class WastCommand permits WastInvalid, WastModule, WastReturn, WastTrap {
    private String line;
    private String type;

    public WastCommand() {}
    public String getLine() {
        return line;
    }
    public void setLine(String line) {this.line = line;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
}
