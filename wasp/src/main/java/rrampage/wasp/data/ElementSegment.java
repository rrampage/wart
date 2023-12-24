package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.instructions.RefTypeInstruction;

public sealed interface ElementSegment {
    record ActiveElementSegment(int tableIndex, ConstInstruction.IntConst initExpr, ValueType.RefType type, boolean isExpression,
                                int[] functionIndexVector, RefTypeInstruction[] expressionVector) implements ElementSegment {}
    record PassiveElementSegment(ValueType.RefType type, boolean isExpression, int[] functionIndexVector,
                                 RefTypeInstruction[] expressionVector) implements ElementSegment {}
    record DeclarativeElementSegment(ValueType.RefType type, boolean isExpression, int[] functionIndexVector,
                                     RefTypeInstruction[] expressionVector) implements ElementSegment {}

    ValueType.RefType type();
    boolean isExpression();
    int[] functionIndexVector();
    // TODO : Change to one of the ref type instructions: ref.func
    RefTypeInstruction[] expressionVector();
}
