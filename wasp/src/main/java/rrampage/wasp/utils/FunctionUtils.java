package rrampage.wasp.utils;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.function.*;

public class FunctionUtils {
    private BiConsumer createVoidHandlerLambda(Object bean, Method method) throws Throwable {
        MethodHandles.Lookup caller = MethodHandles.lookup();
        CallSite site = LambdaMetafactory.metafactory(caller,
                "accept",
                MethodType.methodType(BiConsumer.class),
                MethodType.methodType(void.class, Object.class, Object.class),
                caller.findVirtual(bean.getClass(), method.getName(),
                        MethodType.methodType(void.class, method.getParameterTypes()[0])),
                MethodType.methodType(void.class, bean.getClass(), method.getParameterTypes()[0]));
        MethodHandle factory = site.getTarget();
        BiConsumer listenerMethod = (BiConsumer) factory.invoke();
        return listenerMethod;
    }

    public static <F> F createWrapper(final MethodHandles.Lookup lookup,
                                       final MethodHandle original,
                                       final String lambdaName,
                                       final Class<F> interfaceType,
                                       final Method wrapperMethod) throws Exception{
        final CallSite site = LambdaMetafactory.metafactory(lookup, // MethodHandles.Lookup
                lambdaName, // Name of lambda method from interface
                MethodType.methodType(interfaceType), // MethodType of interface
                MethodType.methodType(wrapperMethod.getReturnType(), wrapperMethod.getParameterTypes()), //Signature of wrapper lambda
                original, // MethodHandle
                original.type()); //Actual signature of method
        try {
            return (F) site.getTarget().invoke();
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable e) {
            throw new Error(e);
        }
    }

    public static void lambdaMetaFactoryExample() throws Throwable {
        // from https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/
        String toBeTrimmed = " text with spaces ";
        Method reflectionMethod = String.class.getMethod("trim");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle handle = lookup.unreflect(reflectionMethod);
        CallSite callSite = LambdaMetafactory.metafactory(
                // method handle lookup
                lookup,
                // name of the method defined in the target functional interface
                "get",
                // type to be implemented and captured objects
                // in this case the String instance to be trimmed is captured
                MethodType.methodType(Supplier.class, String.class),
                // type erasure, Supplier will return an Object
                MethodType.methodType(Object.class),
                // method handle to transform
                handle,
                // Supplier method real signature (reified)
                // trim accepts no parameters and returns String
                MethodType.methodType(String.class));
        Supplier<String> lambda = (Supplier<String>) callSite.getTarget().bindTo(toBeTrimmed).invoke();
    }

    public static MethodHandle generateMethodHandle(IntUnaryOperator op) {
        try {
            var method = op.getClass().getMethod("applyAsInt", int.class);
            return getLookup(op.getClass()).unreflect(method).bindTo(op);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <F> MethodHandle generateMethodHandleForLambda(F lambda) {
        try {
            Class<?> clazz = lambda.getClass();
            Method[] methods = clazz.getDeclaredMethods();
            // As lambdas should only have one declared method
            if (methods.length != 1) {
                throw new RuntimeException("Invalid lambda passed");
            }
            return getLookup(lambda.getClass()).unreflect(methods[0]).bindTo(lambda);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandles.Lookup getLookup(Class<?> targetClass) {
        MethodHandles.Lookup lookupMe = MethodHandles.lookup();
        try {
            return MethodHandles.privateLookupIn(targetClass, lookupMe);
        } catch (IllegalAccessException e) {
            return lookupMe;
        }
    }
}
