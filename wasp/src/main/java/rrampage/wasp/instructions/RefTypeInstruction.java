package rrampage.wasp.instructions;

import rrampage.wasp.data.ValueType;

public sealed interface RefTypeInstruction extends Instruction {
    record RefNull(ValueType.RefType type) implements RefTypeInstruction {public String opCode() {return "ref.null";}}
    record RefIsNull() implements RefTypeInstruction {public String opCode() {return "ref.is_null";}}
    record RefFunc(int functionIndex) implements RefTypeInstruction {public String opCode() {return "ref.func";}}
    record TableGet(int tableIndex) implements RefTypeInstruction {public String opCode() {return "table.get";}}
    record TableSet(int tableIndex) implements RefTypeInstruction {public String opCode() {return "table.set";}}
    record TableCopy(int tableIndex) implements RefTypeInstruction {public String opCode() {return "table.copy";}}
    record TableInit(int tableIndex) implements RefTypeInstruction {public String opCode() {return "table.init";}}
    record TableFill(int tableIndex) implements RefTypeInstruction {public String opCode() {return "table.fill";}}
    record TableSize(int tableIndex) implements RefTypeInstruction {public String opCode() {return "table.size";}}
    record TableGrow(int tableIndex) implements RefTypeInstruction {public String opCode() {return "table.grow";}}
}
