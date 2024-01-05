package rrampage.wasp.instructions;

public sealed interface ConstExpression extends Instruction permits
        ConstInstruction.IntConst,
        GlobalInstruction.GlobalGet,
        RefTypeInstruction.RefFunc,
        RefTypeInstruction.RefNull {
    static ConstExpression[] of(ConstExpression ... args) {return args;}
}
