package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstExpression;

import java.util.Arrays;

public sealed interface ElementSegment {
    record ActiveElementSegment(int tableIndex, ConstExpression initExpr, ValueType.RefType type, boolean isExpression,
                                int[] functionIndexVector, ConstExpression[] expressionVector) implements ElementSegment {
        public String toString() {
            return String.format("ACTIVE_ELEM_SEGMENT: tableIndex: %d initExpression: %s reftype: %s isExpression: %b funcIdxVec: %s exprVec: %s",
                    tableIndex, initExpr, type, isExpression, Arrays.toString(functionIndexVector), Arrays.toString(expressionVector));
        }
    }
    record PassiveElementSegment(ValueType.RefType type, boolean isExpression, int[] functionIndexVector,
                                 ConstExpression[] expressionVector) implements ElementSegment {
        public String toString() {
            return String.format("PASSIVE_ELEM_SEGMENT: reftype: %s isExpression: %b funcIdxVec: %s exprVec: %s",
                    type, isExpression, Arrays.toString(functionIndexVector), Arrays.toString(expressionVector));
        }
    }
    record DeclarativeElementSegment(ValueType.RefType type, boolean isExpression, int[] functionIndexVector,
                                     ConstExpression[] expressionVector) implements ElementSegment {
        public String toString() {
            return String.format("DECLARATIVE_ELEM_SEGMENT: reftype: %s isExpression: %b funcIdxVec: %s exprVec: %s",
                    type, isExpression, Arrays.toString(functionIndexVector), Arrays.toString(expressionVector));
        }
    }

    ValueType.RefType type();
    boolean isExpression();
    int[] functionIndexVector();
    ConstExpression[] expressionVector();
}
