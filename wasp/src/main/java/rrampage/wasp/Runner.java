package rrampage.wasp;

import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.FunctionUtils;

import java.nio.file.Paths;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class Runner {
    public static void main(String[] args) throws Throwable {
        // TODO Load and run wasm file. How to handle imports?
        // (import "host" "print" (func $hprint (param i32) (result i32)))
        String fileName = "./examples/call_indirect_example.wasm";
        String path = Paths.get(fileName).toAbsolutePath().normalize().toString();
        var parser = WasmParser.fromFile(path);
        var module = parser.parseModule();
        var lambda = (IntUnaryOperator) (i) -> i*3;
        var mh = FunctionUtils.generateMethodHandle(lambda);
        var mh2 = FunctionUtils.generateMethodHandleForLambda(lambda);
        System.out.println(mh.type());
        System.out.println(mh2.type());
        System.out.println(mh2.invoke(9));
        var machine = module.instantiate(Map.of("env", Map.of("jstimes3", mh)));
        machine.start();
    }
}
