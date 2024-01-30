package rrampage.wasp.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import rrampage.wasp.instructions.ConstInstruction;

import java.util.Arrays;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = WastCommand.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WastCommand.WastReturn.class, name = "assert_return"),
        @JsonSubTypes.Type(value = WastCommand.WastTrap.class, name = "assert_trap"),
        @JsonSubTypes.Type(value = WastCommand.WastTrap.class, name = "assert_exhaustion"),
        @JsonSubTypes.Type(value = WastCommand.WastModule.class, name = "module"),
        @JsonSubTypes.Type(value = WastCommand.WastInvalid.class, name = "assert_invalid"),
        @JsonSubTypes.Type(value = WastCommand.WastInvalid.class, name = "assert_malformed"),
        @JsonSubTypes.Type(value = WastCommand.WastInvalid.class, name = "assert_uninstantiable"),
        @JsonSubTypes.Type(value = WastCommand.WastInvalid.class, name = "assert_unlinkable"),
})
public sealed class WastCommand permits WastCommand.WastInvalid, WastCommand.WastModule, WastCommand.WastReturn, WastCommand.WastTrap {
    private String line;
    private String type;

    public WastCommand() {}
    public String getLine() {
        return line;
    }
    public void setLine(String line) {this.line = line;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public static final class WastInvalid extends WastCommand {
        @JsonProperty("module_type")
        private String moduleType;
        private String filename;
        private String text;
        public WastInvalid() {}
        public String getModuleType() {
            return moduleType;
        }
        public String getFilename() {
            return filename;
        }
        public String getText() {
            return text;
        }
        public void setText(String text) { this.text = text;}
        public void setFilename(String filename) { this.filename = filename;}
        public void setModuleType(String moduleType) { this.moduleType = moduleType;}
    }

    public static final class WastModule extends WastCommand {
        private String module;
        private String filename;

        public WastModule() {}
        public String getModule() {
            return module;
        }
        public void setModule(String module) { this.module = module;}
        public String getFilename() {
            return filename;
        }
        public void setFilename(String filename) { this.filename = filename;}
    }

    public static final class WastTrap extends WastCommand {
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

    public static record WastAction(String type, String field, WastValue[] args) {}

    public static record WastValue(String type, String value) {
        public ConstInstruction toConst() {
            return switch (type) {
                case "i32" -> (value.startsWith("-")) ?
                        new ConstInstruction.IntConst(Integer.parseInt(value)) :
                        new ConstInstruction.IntConst(Integer.parseUnsignedInt(value));
                case "i64" -> (value.startsWith("-")) ?
                        new ConstInstruction.LongConst(Long.parseLong(value)) :
                        new ConstInstruction.LongConst(Long.parseUnsignedLong(value));
                // For floats, the numbers are written as the decimal encoding of the binary representation of the number
                case "f32" -> switch (value) {
                    case "nan:canonical" -> ConstInstruction.NAN_CANONICAL_FLOAT;
                    case "nan:arithmetic" -> ConstInstruction.NAN_ARITHMETIC_FLOAT;
                    default -> new ConstInstruction.FloatConst(Float.intBitsToFloat(Integer.parseUnsignedInt(value)));
                };
                case "f64" -> switch (value) {
                    case "nan:canonical" -> ConstInstruction.NAN_CANONICAL_DOUBLE;
                    case "nan:arithmetic" -> ConstInstruction.NAN_ARITHMETIC_DOUBLE;
                    default -> new ConstInstruction.DoubleConst(Double.longBitsToDouble(Long.parseUnsignedLong(value)));
                };
                default -> throw new RuntimeException("Invalid type passed: " + type);
            };
        }
    }

    public static final class WastReturn extends WastCommand {
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
            return new AssertReturn(action.field(), getLine(), args, expected);
        }
    }
}
