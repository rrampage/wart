package rrampage.wasp.examples;

import rrampage.wasp.data.Module;
import rrampage.wasp.data.*;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.FileUtils;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitors;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class Waforth {
    private static final int PAD_OFFSET = 400;
    Module module;
    Machine machine;
    final Memory memory;
    final Table table;
    final Scanner scanner;
    final Map<String, Consumer<Waforth>> functions;
    String inpBuffer;
    StringBuilder keyBuffer;
    boolean debug = false;

    public Waforth() throws IOException {
        this.module = WasmParser.fromFile("../wart/examples/waforth/waforth.wasm").parseModule();
        this.machine = module.instantiate(createImportMap(), MachineVisitors.NULL_VISITOR);
        this.memory = (Memory) machine.exports().get("memory");
        this.table = (Table) machine.exports().get("table");
        this.scanner = new Scanner(System.in);
        this.inpBuffer = "";
        this.keyBuffer = new StringBuilder();
        this.functions = new HashMap<>();
    }

    public int pop() {
        machine.invoke("pop");
        return machine.popInt();
    }

    public String popString() {
        var len = this.pop();
        var addr = this.pop();
        System.out.println(STR."ADDR: \{addr} LEN: \{len}");
        return ProgramUtils.loadString(this.memory, addr, len);
    }

    private int here() {
        machine.invoke("here");
        return machine.popInt();
    }

    public void push(int n) {
        machine.invoke("push", constOf(n));
    }

    public int pushString(String s, int offset) {
        var addr = this.here() + PAD_OFFSET;
        var len =  ProgramUtils.saveString(s, memory, addr);
        this.push(addr);
        this.push(len);
        System.out.println(STR."ADDR: \{addr} LEN: \{len}");
        return addr + PAD_OFFSET;
    }

    public void read(String s) {
        inpBuffer = inpBuffer + s;
    }

    private void run(boolean silent) {
        machine.invoke("run", constOf(silent? 1 : 0));
    }

    public void interpret(String s, boolean silent) {
        if (!s.endsWith("\n")) {
            s = s + "\n";
        }
        read(s);
        try {
            run(silent);
        } catch (Exception e) {
            machine.invoke("error");
            System.out.println(STR."RUN_ERROR: \{e.getMessage()} ERR_CODE: \{machine.popInt()}");
        }
    }

    public void bind(String name, Consumer<Waforth> f) {
        this.functions.put(name, f);
    }

    private void call() {
        var len = pop();
        var addr = pop();
        var fname = ProgramUtils.loadString(memory, addr, len);
        var fn = this.functions.get(fname);
        if (fn == null) {
            throw new RuntimeException("CALL_ERROR: Unbound SCALL " + fname);
        }
        fn.accept(this);
    }

    private void emit(int c) {
        System.out.print((char)c);
    }

    private int key() {
        while (keyBuffer.isEmpty()) {
            String s = scanner.next();
            keyBuffer.append(s);
            if (s.length() != 1) {
                keyBuffer.append('\n');
            }
        }
        char c = keyBuffer.charAt(0);
        System.out.print(c);
        keyBuffer = new StringBuilder(keyBuffer.substring(1));
        return c;
    }

    private int readInputBuffer(int addr, int length) {
        var i = inpBuffer.indexOf('\n');
        String input;
        if (i == -1) {
            input = inpBuffer;
            inpBuffer = "";
        } else {
            input = inpBuffer.substring(0, i+1);
            inpBuffer = inpBuffer.substring(i+1);
        }
        // System.out.println(STR."READ_STR: \{input}");
        // System.out.println(STR."BUFFER: \{inpBuffer}");
        return ProgramUtils.saveString(input, memory, addr);
    }

    private void load(int offset, int length) {
        byte[] data = memory.load(offset, length);
        Module newMod = new WasmParser(data).parseModule();
        if (debug) {
            FileUtils.writeBinaryFile(STR."./examples/tmp/waforth-\{System.currentTimeMillis()}.wasm", data);
        }
        Machine newMachine = newMod.instantiate(createImportMapForForthWord(), MachineVisitors.logVisitor());
    }

    private Map<String, Map<String,Object>> createImportMap() throws RuntimeException {
        try {
            FunctionType intBiFunction = FunctionType.I32_BINARY;
            FunctionType intBiConsume = FunctionType.I32_BICONSUME;
            var callMh = Function.createImportFunction("call", FunctionType.VOID,
                    MethodHandles.lookup().findVirtual(this.getClass(), "call", FunctionType.getMethodTypeFromFunctionType(FunctionType.VOID)).bindTo(this));
            var emitMh = Function.createImportFunction("emit", FunctionType.I32_CONSUME,
                    MethodHandles.lookup().findVirtual(this.getClass(), "emit", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_CONSUME)).bindTo(this));
            var keyMh = Function.createImportFunction("key", FunctionType.I32_RETURN,
                    MethodHandles.lookup().findVirtual(this.getClass(), "key", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_RETURN)).bindTo(this));
            var readMh = Function.createImportFunction("read", intBiFunction,
                    MethodHandles.lookup().findVirtual(this.getClass(), "readInputBuffer", FunctionType.getMethodTypeFromFunctionType(intBiFunction)).bindTo(this));
            var loadMh = Function.createImportFunction("load", intBiConsume,
                    MethodHandles.lookup().findVirtual(this.getClass(), "load", FunctionType.getMethodTypeFromFunctionType(intBiConsume)).bindTo(this));
            return Map.of("shell", Map.of("call", callMh, "emit", emitMh, "key", keyMh, "read", readMh, "load", loadMh));
        } catch (Exception e) {
            throw new RuntimeException("WAFORTH_INIT Method Handle lookup failed");
        }
    }

    private Map<String, Map<String,Object>> createImportMapForForthWord() {
        return Map.of("env", Map.of("table", table, "memory", memory));
    }

    public static void main(String[] args) throws Exception {
        /*
              ;; Write a character to the output device
              (import "shell" "emit" (func $shell_emit (param i32)))

              ;; Read input from input device
              ;; Parameters: target address, maximum size
              ;; Returns: number of bytes read
              (import "shell" "read" (func $shell_read (param i32 i32) (result i32)))

              ;; Read a single key from the input device (without echoing)
              (import "shell" "key" (func $shell_key (result i32)))

              ;; Load a webassembly module.
              ;; Parameters: WASM bytecode memory offset, size
              (import "shell" "load" (func $shell_load (param i32 i32)))

              ;; Generic signal to shell
              (import "shell" "call" (func $shell_call))
            Export[11]:
             - func[5] <run> -> "run"
             - table[0] -> "table"
             - memory[0] -> "memory"
             - func[254] <leb128_4p> -> "leb128_4p"
             - func[255] <leb128> -> "leb128"
             - func[256] <leb128u> -> "leb128u"
             - func[269] <push> -> "push"
             - func[270] <pop> -> "pop"
             - func[271] <tos> -> "tos"
             - func[272] <here> -> "here"
             - func[273] <error> -> "error"

         */
        Waforth forth = new Waforth();
        var s = """
: SAY_HELLO
  ." Hello, Forth" CR
;
( Call "prompt" with the given string )
: PROMPT ( c-addr u -- n )
  S" prompt" SCALL
;

( Prompt the user for a number, and write it to output )
: ASK-NUMBER ( -- )
  S" Please enter a number" PROMPT DUP
  ." The number was " . CR
;

( Calculate factorial of number)
: FACTORIAL ( n -- n! )   1 SWAP 1+ 1 DO I * LOOP ;

: prime? HERE + C@ 0= ;
: composite! HERE + 1 SWAP C! ;

: sieve
  HERE OVER ERASE
  2
  BEGIN
    2DUP DUP * >
  WHILE
    DUP prime? IF
      2DUP DUP * DO
        I composite!
      DUP +LOOP
    THEN
    1+
  REPEAT
  DROP
  1 SWAP 2 DO I prime? IF DROP I THEN LOOP .
;
                """;
        forth.interpret(s, true);
        Scanner scanner = new Scanner(System.in);
        forth.bind("prompt", (stack) -> {
            var message = stack.popString();
            System.out.println(message);
            var result = scanner.nextLine();
            try {
                System.out.println("Bind");
                stack.push(Integer.parseInt(result));
            } catch (Exception _) {}
        });
        // forth.interpret("ASK-NUMBER", true);
        var result = "";
        System.out.println("Welcome to Waforth running on Wasp!");
        System.out.println("Press q or quit to exit REPL");
        for (int i = 0; i < 100; i++) {
            result = scanner.nextLine();
            if (result.equalsIgnoreCase("Q") || result.equalsIgnoreCase("quit")) {
                forth.interpret(".S", true);
                break;
            }
            forth.interpret(result, true);
            forth.interpret(".S", true);
            System.out.println();
        }
    }
}
