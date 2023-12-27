package rrampage.wasp.instructions;

public sealed interface ConstExpression extends Instruction permits ConstInstruction.IntConst, GlobalInstruction.GlobalGet, RefTypeInstruction.RefFunc, RefTypeInstruction.RefNull {}
