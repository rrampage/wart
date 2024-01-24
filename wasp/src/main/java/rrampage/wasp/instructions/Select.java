package rrampage.wasp.instructions;

import rrampage.wasp.data.ValueType;

public sealed interface Select extends Instruction {
    record SelectUntyped() implements Select { public String opCode() {
            return "select";
        }}
    record SelectTyped(ValueType[] types) implements Select {public String opCode() {
        return "select_t";
    }}
}
