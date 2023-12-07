package rrampage.waja.wasm.parser;

import rrampage.waja.wasm.Module;
import rrampage.waja.wasm.data.DataType;
import rrampage.waja.wasm.data.Function;
import rrampage.waja.wasm.data.FunctionType;
import rrampage.waja.wasm.instructions.Instruction;
import rrampage.waja.wasm.parser.repr.*;

import java.util.*;

public class WatParser {

    private final String input;
    private final HashMap<String, Function> functions = new HashMap<>();

    public WatParser(String input) {
        this.input = input;
    }

    private ConsList parseTokens() {
        ConsList c = new ConsList(new ArrayList<>());
        parseTokens(0, c);
        if (c.val().size() == 1 && Objects.requireNonNull(c.val().get(0)) instanceof ConsList cl) {
            c = cl;
        }
        //System.out.println(c);
        /*Function f = parseFunction(c);
        if (f != null) {
            System.out.println(f);
        }*/
        return c;
    }

    private int parseTokens(int i, ConsList l) {
        StringBuilder sb = new StringBuilder();
        while (i < input.length() && i >= 0) {
            char ch = input.charAt(i);
            i++;
            switch (ch) {
                case '(' -> {
                    if (!sb.isEmpty()) {
                        String s = sb.toString().trim();
                        if (!s.isBlank()) {
                            l.val().add(new ConsAtom(s));
                            sb = new StringBuilder();
                        }
                    }
                    ConsList cl = new ConsList(new ArrayList<>());
                    i = parseTokens(i, cl);
                    // System.out.println("Index: " + i + " length: " + input.length());
                    l.val().add(cl);
                }
                case ')' -> {
                    if (!sb.isEmpty()) {
                        String s = sb.toString().trim();
                        if (!s.isBlank()) {
                            l.val().add(new ConsAtom(s));
                        }
                    }
                    return i;
                }
                case ',' -> {}
                default -> sb.append(ch);
            }
        }
        return -1;
    }

    private String stripLineComment(ConsAtom c) {
        return c.val().split(" *;; *")[0];
    }

    private boolean parseBlockComment(ConsList cl) {
        // (; cdhvbf ;)
        return cl.startsWith(";") && cl.val().size() == 1
                && (cl.val().getFirst() instanceof ConsAtom)
                && ((ConsAtom) cl.val().getFirst()).val().endsWith(";");
    }

    public Module parseModule() {
        // Assuming that this parser will parse well-formed modules
        ConsList cl = parseTokens();
        // skip comments - line and block and just get the module
        if (cl.val().size() != 1) {
            // Get conslist which starts with module
            for (Cons c : cl.val()) {
                if (c instanceof ConsList cc && cc.startsWith("module")) {
                    cl = cc;
                    break;
                }
            }
        }
        System.out.println(cl.val().size());
        if (!cl.startsWith("module")) {
            return null;
        }
        ArrayList<FunctionType> types = new ArrayList<>();
        ArrayList<Function> functions = new ArrayList<>();
        HashMap<String, Integer> imports = new HashMap<>();

        for (Cons c : cl.val()) {
            switch (c) {
                case ConsList x -> {
                    if (parseBlockComment(x)) {
                        continue;
                    }
                    var ft = parseType(x);
                    if (ft != null) {
                        types.add(ft);
                        continue;
                    }
                    var f = parseFunction(x);
                    if (f != null) {
                        functions.add(f);
                        continue;
                    }
                    // imports
                    var ifun = parseImport(x, types);
                    if (ifun != null) {
                        functions.add(ifun);
                        imports.put(ifun.name(), functions.size()-1);
                        continue;
                    }
                    // TODO exports
                }
                case ConsAtom x -> {

                }
            }
        }

        return new Module(
                types.toArray(new FunctionType[]{}),
                functions.toArray(new Function[]{}),
                new HashMap<>(),
                imports);
    }

    private FunctionType parseFunctionType(ConsList cl) {
        /*
            (func (param i32 i32 i32 i32 i32 i32) (result i32)))
         */
        if (!cl.startsWith("func")) {
            return null;
        }
        ArrayList<DataType> paramTypes = new ArrayList<>();
        ArrayList<DataType> returnTypes = new ArrayList<>();
        for (Cons c : cl.val()) {
            if (c instanceof ConsList x) {
                if (parseBlockComment(x)) {
                    continue;
                }
                var p = parseDataTypes("param", x);
                if (!p.isEmpty()) {
                    paramTypes.addAll(p);
                    continue;
                }
                var r = parseDataTypes("result", x);
                if (!r.isEmpty()) {
                    returnTypes.addAll(r);
                }
            }
        }
        return new FunctionType(paramTypes.toArray(new DataType[]{}), returnTypes.toArray(new DataType[]{}));
    }

    private int parseTypeIdx(ConsList cl) {
        int idx = -1;
        if (!cl.startsWith("type")) {
            return idx;
        }
        for (Cons c : cl.val()) {
            switch (c) {
                case ConsAtom x -> {
                    if (!x.val().isBlank() && x.val().split(" ").length >= 2) {
                        try {
                            return Integer.parseInt(x.val().split(" ")[1]);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }
                case ConsList x -> {

                }
            }
        }
        return idx;
    }

    private FunctionType parseType(ConsList cl) {
        /*
              (type (;15;) (func (param i32 i32 i32 i32 i32 i32) (result i32)))
         */
        if (!cl.startsWith("type")) {
            return null;
        }
        for (Cons c : cl.val()) {
            switch (c) {
                case ConsList x -> {
                    if (parseBlockComment(x)) {
                        continue;
                    }
                    var ft = parseFunctionType(x);
                    if (ft != null) {
                        return ft;
                    }
                }
                case ConsAtom consAtom -> {}
            }
        }
        return null;
    }

    private Function parseFunction(ConsList cl) {
        if (!cl.startsWith("func")) {
            return null;
        }
        ArrayList<DataType> paramTypes = new ArrayList<>();
        ArrayList<DataType> locals = new ArrayList<>();
        ArrayList<DataType> returnTypes = new ArrayList<>();
        String funcName = "";
        Instruction[] code = null;
        for (Cons c : cl.val()) {
            switch (c) {
                case ConsAtom(String x) -> {
                    // Setting function name
                    if (funcName.isBlank() && !x.contains("\n") && x.split(" ").length >= 2) {
                        funcName = x.split(" ")[1];
                        continue;
                    }
                    // TODO: Parse instructions
                    if (x.contains("\n")) {
                        code = Instruction.parse(x);
                    }
                }
                case ConsList x -> {
                    if (parseBlockComment(x)) {
                        continue;
                    }
                    var p = parseDataTypes("param", x);
                    if (!p.isEmpty()) {
                        paramTypes.addAll(p);
                        continue;
                    }
                    var r = parseDataTypes("result", x);
                    if (!r.isEmpty()) {
                        returnTypes.addAll(r);
                        continue;
                    }
                    var l = parseDataTypes("local", x);
                    if (!l.isEmpty()) {
                        locals.addAll(l);
                        continue;
                    }
                    var i = parseTypeIdx(x);
                    if (i >= 0) {
                        // System.out.println("Type idx: " + i);
                        // TODO: Type check??
                    }
                }
            }
        }
        Function f = new Function(funcName,
                new FunctionType(paramTypes.toArray(new DataType[]{}), returnTypes.toArray(new DataType[]{})), locals.toArray(new DataType[]{}), code);
        functions.put(funcName, f);
        return f;
    }

    private List<DataType> parseDataTypes(String term, ConsList cl) {
        if (!cl.startsWith(term)) {
            return List.of();
        }
        ConsAtom c = (ConsAtom) Objects.requireNonNull(cl.val().get(0));
        String[] ss = c.val().split(" +");
        List<DataType> params = new ArrayList<>();
        for (int i = 1; i < ss.length; i++) {
            parseDataType(ss[i]).ifPresent(params::add);
        }
        return params;
    }

    public Optional<DataType> parseDataType(String s) {
        try {
            return Optional.of(DataType.valueOf(s.toUpperCase()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Function parseImport(ConsList cl, ArrayList<FunctionType> types) {
        /*
            TODO
            (import "host" "print" (func $hprint (param i32) (result i32)))
            (import "env" "Math_atan" (func (;0;) (type 5)))
         */
        if (!cl.startsWith("import")) {
            return null;
        }
        String[] importData = ((ConsAtom) cl.val().get(0)).val().split(" +");
        if (importData.length < 3) {
            return null;
        }
        String importName = importData[1];
        String functionName = importData[2];
        for (Cons c : cl.val().subList(1, cl.val().size())) {
            if (c instanceof ConsList x) {
                if (parseBlockComment(x)) {
                    continue;
                }
                var ft = parseFunctionType(x);
                if (ft != null) {
                    return Function.createStubFunction(importName+"::" + functionName, ft);
                }
                for (Cons cc : x.val()) {
                    if (cc instanceof ConsList xx) {
                        if (parseBlockComment(xx)) {
                            continue;
                        }
                        var i = parseTypeIdx(x);
                        if (i >= 0 && types != null && i < types.size()) {
                            System.out.println("Type idx for import function: " + i);
                            return Function.createStubFunction(importName+"::" + functionName, types.get(i));
                        }
                    }
                }
            }
        }
        return null;
    }

    private void parseTable(ConsList cl) {
        /*
            TODO
            (table (;0;) 59 59 funcref)
         */
    }

    public static void main(String[] args) {
        // Parse some programs!
        /*
            1. Match ( )
            2. func
            3. param
            4. result
            5. module
            6. local
            7. global
            8. blocks
         */
        String program = """
                (func $offsetFromCoordinate (;2;) (param $x i32) (param $y i32) (result i32)
                  get_local $y
                  i32.const 50
                  i32.mul
                  get_local $x
                  i32.add
                  i32.const 4
                  i32.mul
                )
                """;
        System.out.println(program);
        WatParser watParser = new WatParser(program);
        ConsList cl = watParser.parseTokens();
        System.out.println(watParser.functions);
    }
}
