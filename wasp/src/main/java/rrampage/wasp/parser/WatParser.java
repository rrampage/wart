package rrampage.wasp.parser;

import rrampage.wasp.data.*;
import rrampage.wasp.data.Module;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.parser.repr.*;
import rrampage.wasp.parser.types.ExportMetadata;
import rrampage.wasp.parser.types.ImportMetadata;

import java.util.*;

public record WatParser(String input) implements Parser {

    private ConsList parseTokens() {
        ConsList c = new ConsList(new ArrayList<>());
        parseTokens(0, c);
        if (c.val().size() == 1 && Objects.requireNonNull(c.val().get(0)) instanceof ConsList cl) {
            c = cl;
        }
        //System.out.println(c);
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
        // block ;; label = @3
        return c.val().split(" *;; *", 2)[0];
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
                    var f = parseFunction(x, types);
                    if (f != null) {
                        functions.add(f);
                        continue;
                    }
                    // imports
                    var ifun = parseImport(x, types);
                    if (ifun != null) {
                        functions.add(ifun);
                        //imports.put(ifun.name(), functions.size()-1);
                        continue;
                    }
                    // TODO exports
                }
                case ConsAtom x -> {

                }
            }
        }

        return new Module(1,
                types.toArray(new FunctionType[]{}),
                functions.toArray(new Function[]{}),
                new Table[]{},
                new ExportMetadata[]{},
                new ImportMetadata[]{},
                null,
                null,
                null,
                -1
        );
    }

    private FunctionType parseFunctionType(ConsList cl) {
        /*
            (func (param i32 i32 i32 i32 i32 i32) (result i32)))
         */
        if (!cl.startsWith("func")) {
            return null;
        }
        ArrayList<ValueType.NumType> paramTypes = new ArrayList<>();
        ArrayList<ValueType.NumType> returnTypes = new ArrayList<>();
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
        return new FunctionType(paramTypes.toArray(new ValueType.NumType[]{}), returnTypes.toArray(new ValueType.NumType[]{}));
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

    private Function parseFunction(ConsList cl, ArrayList<FunctionType> types) {
        /*
            Example Data:
            (func (;14;) (type 0) (param i32) (local i32 i32 i32 i32) .... )
            (func (;11;) (type 3) (param i32 i32 i32) (local i32)... )
         */
        if (!cl.startsWith("func")) {
            return null;
        }
        /*String cls = cl.toString();
        System.out.println(cls.substring(0, Math.min(cls.length(), 100)));*/
        ArrayList<ValueType.NumType> paramTypes = new ArrayList<>();
        ArrayList<ValueType.NumType> locals = new ArrayList<>();
        ArrayList<ValueType.NumType> returnTypes = new ArrayList<>();
        String funcName = "";
        Instruction[] code = null;
        FunctionType ft = null;
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
                    if (i >= 0 && types != null && i < types.size()) {
                        // System.out.println("Type idx: " + i);
                        if (ft == null) {
                            ft = types.get(i);
                        }
                        continue;
                    }
                }
            }
        }
        FunctionType type = new FunctionType(paramTypes.toArray(new ValueType.NumType[]{}), returnTypes.toArray(new ValueType.NumType[]{}));
        if (ft != null && !type.equals(ft)) {
            throw new RuntimeException("Type index in module does not match" + ft + " " + type);
        }
        // TODO Fill labels correctly from block/loop instructions
        return new Function(funcName, type, locals.toArray(new ValueType.NumType[]{}), code, null);
    }

    private List<ValueType.NumType> parseDataTypes(String term, ConsList cl) {
        if (!cl.startsWith(term)) {
            return List.of();
        }
        ConsAtom c = (ConsAtom) Objects.requireNonNull(cl.val().get(0));
        String[] ss = c.val().split(" +");
        List<ValueType.NumType> params = new ArrayList<>();
        for (int i = 1; i < ss.length; i++) {
            parseDataType(ss[i]).ifPresent(params::add);
        }
        return params;
    }

    public Optional<ValueType.NumType> parseDataType(String s) {
        try {
            return Optional.of(ValueType.NumType.valueOf(s.toUpperCase()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Function parseImport(ConsList cl, ArrayList<FunctionType> types) {
        /*
            Example data:
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
    }
}
