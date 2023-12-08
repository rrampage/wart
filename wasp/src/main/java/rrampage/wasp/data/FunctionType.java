package rrampage.wasp.data;

import java.lang.invoke.MethodType;
import java.util.Arrays;

public record FunctionType(DataType[] paramTypes, DataType[] returnTypes) {
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
        if (obj == null || !(obj instanceof FunctionType ft)) {
            return false;
        }
        if (this.isVoidReturn() != ft.isVoidReturn() || this.numParams() != ft.numParams()) {
            return false;
        }
        return Arrays.equals(this.paramTypes, ft.paramTypes) && Arrays.equals(this.returnTypes, ft.returnTypes);
    }

    public static Class<?> getClassFromDataType(DataType d) {
        return switch (d) {
            case null -> void.class;
            case I32 -> int.class;
            case I64 -> long.class;
            case F32 -> float.class;
            case F64 -> double.class;
        };
    }

    public static MethodType getMethodTypeFromFunctionType(FunctionType ft) {
        Class<?> rtype = ft.isVoidReturn() ? void.class : getClassFromDataType(ft.returnTypes()[0]);
        Class<?>[] ptypes = (ft.numParams() == 0) ?
                new Class<?>[]{void.class} :
                Arrays.stream(ft.paramTypes()).map(FunctionType::getClassFromDataType).toArray(Class<?>[]::new);
        return MethodType.methodType(rtype, ptypes);
    }
}
