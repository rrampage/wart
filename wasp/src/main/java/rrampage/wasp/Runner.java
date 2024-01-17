package rrampage.wasp;

import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.ValueType;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.ImportUtils;
import rrampage.wasp.vm.MachineVisitors;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class Runner {

    public static long fibonacci(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        long a = 0, b = 1;
        for (int i = 2; i <=n ; i++) {
            long t = a;
            t = b;
            b = a + b;
            a = t;
        }
        return b;
    }

    public static void main(String[] args) throws Exception {
        // TODO Load and run wasm file. How to handle imports?
        // (import "host" "print" (func $hprint (param i32) (result i32)))
        String fileName = "./examples/testsuite/fac.0.wasm";
        String path = Paths.get(fileName).toAbsolutePath().normalize().toString();
        var parser = WasmParser.fromFile(path);
        var module = parser.parseModule();
        var machine = module.instantiate(
                Map.of("host", Map.of("print", ImportUtils.generateLoggerHandle(new FunctionType(new ValueType.NumType[]{ValueType.NumType.I32}, new ValueType.NumType[]{ValueType.NumType.I32})))),
                MachineVisitors.instructionCountVisitor()
        );
        machine.invoke("fac-iter", constOf(21L));
        var rand = ThreadLocalRandom.current();
        for (int i = 0; i < 1000; i++) {
            int n = rand.nextInt(80);
            System.out.println("n:" + n + " fib(n): " + fibonacci(n));
        }
    }
}
