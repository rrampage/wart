package rrampage.wasp.instructions;

public sealed interface ConstExpression extends Instruction permits
        ConstInstruction.IntConst,
        ConstInstruction.LongConst,
        ConstInstruction.FloatConst,
        ConstInstruction.DoubleConst,
        GlobalInstruction.GlobalGet,
        RefTypeInstruction.RefFunc,
        RefTypeInstruction.RefNull {
    static ConstExpression[] of(ConstExpression ... args) {return args;}
}
