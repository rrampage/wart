package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstInstruction;

public record WastValue(String type, String value) {
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
            default -> throw new RuntimeException("Invalid type passed");
        };
    }
}
