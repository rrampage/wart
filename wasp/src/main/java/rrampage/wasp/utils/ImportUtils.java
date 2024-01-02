package rrampage.wasp.utils;

import rrampage.wasp.data.FunctionType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class ImportUtils {
    public static int log(int val) {
        System.out.println(val);
        return val;
    }

    public static long log(long val) {
        System.out.println(val);
        return val;
    }

    public static float log(float val) {
        System.out.println(val);
        return val;
    }

    public static double log(double val) {
        System.out.println(val);
        return val;
    }

    public static MethodHandle generateLoggerHandle(FunctionType type) throws NoSuchMethodException, IllegalAccessException {
        var methodType = type.getMethodType();
        return MethodHandles.lookup().findStatic(ImportUtils.class, "log", methodType);
    }
}
