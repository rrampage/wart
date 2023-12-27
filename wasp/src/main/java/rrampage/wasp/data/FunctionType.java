package rrampage.wasp.data;

import java.lang.invoke.MethodType;
import java.util.Arrays;

import static rrampage.wasp.data.ValueType.*;
import static rrampage.wasp.data.ValueType.NumType.*;

public record FunctionType(ValueType[] paramTypes, ValueType[] returnTypes) {
    public boolean isVoidReturn() {
        return returnTypes == null || returnTypes.length == 0;
    }
    public int numParams() {
        return paramTypes == null ? 0 : paramTypes.length;
    }
    public String toString() {
        return String.format("Function type signature: params: %s, return: %s",
                (numParams() == 0) ? 0 : Arrays.toString(paramTypes),
                (isVoidReturn()) ? "void" : Arrays.toString(returnTypes)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FunctionType ft)) {
            return false;
        }
        if (this.isVoidReturn() != ft.isVoidReturn() || this.numParams() != ft.numParams()) {
            return false;
        }
        return Arrays.equals(this.paramTypes, ft.paramTypes) && Arrays.equals(this.returnTypes, ft.returnTypes);
    }

    public static Class<?> getClassFromDataType(ValueType d) {
        return switch (d) {
            case null -> void.class;
            case I32 -> int.class;
            case I64 -> long.class;
            case F32 -> float.class;
            case F64 -> double.class;
            default -> throw new IllegalStateException(String.format("Invalid value type: %s", d));
        };
    }

    public static Class<?> getReturnType(FunctionType ft) {
        if (ft.isVoidReturn()) {
            return void.class;
        }
        Class<?> rtype = getClassFromDataType(ft.returnTypes()[0]);
        if (ft.returnTypes().length == 1) {
            return rtype;
        }
        for (int i = 1; i < ft.returnTypes().length; i++) {
            if (!getClassFromDataType(ft.returnTypes()[i]).equals(rtype)) {
                return Object[].class;
            }
        }
        return rtype.arrayType();
    }

    public static MethodType getMethodTypeFromFunctionType(FunctionType ft) {
        Class<?> rtype = getReturnType(ft);
        Class<?>[] ptypes = (ft.numParams() == 0) ?
                new Class<?>[]{} :
                Arrays.stream(ft.paramTypes()).map(FunctionType::getClassFromDataType).toArray(Class<?>[]::new);
        return MethodType.methodType(rtype, ptypes);
    }

    public static FunctionType getBlockType(int blockType) {
        if (blockType >= 0) {
            throw new IllegalArgumentException("Non-negative blocktype found: " + blockType);
        }
        return switch (blockType) {
            case -64 -> VOID;
            case -1 -> I32_RETURN;
            case -2 -> I64_RETURN;
            case -3 -> F32_RETURN;
            case -4 -> F64_RETURN;
            default -> throw new IllegalArgumentException("Invalid negative blocktype found: " + blockType);
        };
    }

    public static final FunctionType VOID = new FunctionType(null, null);
    public static final FunctionType I32_RETURN = new FunctionType(null, new NumType[]{I32});
    public static final FunctionType I64_RETURN = new FunctionType(null, new NumType[]{I64});
    public static final FunctionType F32_RETURN = new FunctionType(null, new NumType[]{F32});
    public static final FunctionType F64_RETURN = new FunctionType(null, new NumType[]{NumType.F64});
}
