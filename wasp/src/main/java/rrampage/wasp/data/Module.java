package rrampage.wasp.data;

import rrampage.wasp.vm.Machine;
import rrampage.wasp.instructions.ConstExpression;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.instructions.GlobalInstruction;
import rrampage.wasp.instructions.RefTypeInstruction;
import rrampage.wasp.parser.types.*;
import rrampage.wasp.vm.MachineVisitor;
import rrampage.wasp.vm.MachineVisitors;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static rrampage.wasp.utils.ConversionUtils.doubleToLong;
import static rrampage.wasp.utils.ConversionUtils.floatToLong;

public record Module(
        int version,
        FunctionType[] types,
        Function[] functions,
        Table[] tables,
        ExportMetadata[] exports,
        ImportMetadata[] imports,
        Memory[] memories,
        DataSegment[] dataSegments,
        ElementSegment[] elementSegments,
        Variable[] globals,
        long startIdx
) {
    public String toString() {
        return String.format("""
                Module:
                    Types: %s
                    Functions: %s
                    Exports: %s
                    Globals: %s
                    Data segments: %d
                """, Arrays.toString(types),
                Arrays.toString(functions),
                Arrays.toString(exports),
                Arrays.toString(globals),
                dataSegments == null ? 0 : dataSegments.length);
    }

    private void processImports(Map<String, Map<String, Object>> importMap) {
        /*
        From JS API, import object looks like:
            const importObject = {
              imports: { // module name
                imported_func(arg) { // import name
                  console.log(arg);
                },
                mem: new WebAssembly.Memory({ initial: 10, maximum: 100 }),
                globalVar: new WebAssembly.Global({ value: "i32", mutable: true }, 0),
                table: new WebAssembly.Table({ initial: 1, element: "funcref" }),
              },
            };
         */
        var icount = imports() == null ? 0 : imports().length;
        if (importMap == null) {
            if (icount == 0) {
                return;
            }
            throw new RuntimeException("INIT_ERROR: All imports not supplied. Missing " + icount + " imports");
        }
        var funcIdx = 0;
        var varIdx = 0;
        var memIdx = 0;
        var tblIdx = 0;
        for (var id : imports) {
            if (!importMap.containsKey(id.module())) {
                throw new RuntimeException(String.format("INIT_ERROR: Unable to find module %s in supplied import", id.module()));
            }
            var map = importMap.get(id.module());
            if (!map.containsKey(id.name())) {
                throw new RuntimeException(String.format("INIT_ERROR: Unable to find import %s in module %s in supplied import", id.name(), id.module()));
            }
            Object o =  map.get(id.name());
            switch (id.importDescriptor()) {
                case ImportDescriptor.FunctionDescriptor d -> functions()[funcIdx++] = switch (o) {
                    case MethodHandle mh -> Function.createImportFunction(id.name(), types()[d.idx()], mh);
                    case Function f -> f;
                    default -> throw new RuntimeException(String.format("INIT_ERROR: Unable to find import name of %s of type %s in module %s in supplied import", id.name(), d, id.module()));
                };
                case ImportDescriptor.GlobalDescriptor d -> {
                    if (!(o instanceof Variable v) || !v.matchesDescriptor(d)) {
                        throw new RuntimeException(String.format("INIT_ERROR: Unable to find import name of %s of type %s in module %s in supplied import", id.name(), d, id.module()));
                    }
                    globals()[varIdx++] = v;
                }
                case ImportDescriptor.MemoryDescriptor d -> {
                    if (!(o instanceof Memory m) || !m.matchesDescriptor(d)) {
                        throw new RuntimeException(String.format("INIT_ERROR: Unable to find import name of %s of type %s in module %s in supplied import", id.name(), d, id.module()));
                    }
                    memories()[memIdx++] = m;
                }
                case ImportDescriptor.TableDescriptor d -> {
                    if (!(o instanceof Table t) || !t.matchesDescriptor(d)) {
                        throw new RuntimeException(String.format("INIT_ERROR: Unable to find import name of %s of type %s in module %s in supplied import", id.name(), d, id.module()));
                    }
                    tables()[tblIdx++] = t;
                }
            }
            icount--;
        }
        if (icount > 0) {
            throw new RuntimeException("INIT_ERROR: All imports not supplied. Missing " + icount + " imports");
        }
    }

    private void processGlobals() {
        for (int  i =0; i < globals().length; i++) {
            var v = globals()[i];
            switch (v.expr()) {
                case ConstInstruction.IntConst e -> globals()[i] = Variable.newVariable(v.type(), e.val(), v.isMutable());
                case ConstInstruction.LongConst e -> globals()[i] = Variable.newVariable(v.type(), e.val(), v.isMutable());
                case ConstInstruction.FloatConst e -> globals()[i] = Variable.newVariable(v.type(), floatToLong(e.val()), v.isMutable());
                case ConstInstruction.DoubleConst e -> globals()[i] = Variable.newVariable(v.type(), doubleToLong(e.val()), v.isMutable());
                case GlobalInstruction.GlobalGet e -> {
                    var src = globals()[e.val()];
                    if (!src.type().equals(v.type())) {
                        throw new RuntimeException(String.format("INIT_ERROR: Type %s of Global at %d does not match expected type %s", src.type(), e.val(), v.type()));
                    }
                    globals()[i] = Variable.newVariable(v.type(), src.getValAsLong(), v.isMutable());
                }
                case RefTypeInstruction.RefFunc e -> globals()[i] = Variable.newVariable(v.type(), e.functionIndex(), v.isMutable());
                case RefTypeInstruction.RefNull e -> globals()[i] = Variable.newVariable(v.type(), Variable.REF_NULL, v.isMutable());
                case null -> throw new RuntimeException(String.format("INIT_ERROR: Null global found at index %d", i));
            }
        }
    }

    private int getOffsetFromConstExpr(ConstExpression expr) {
        return switch (expr) {
            case ConstInstruction.IntConst e -> e.val();
            case GlobalInstruction.GlobalGet e -> {
                var src = globals()[e.val()];
                if (src.type() != Variable.I32Variable.type) {
                    throw new RuntimeException(String.format("INIT_ERROR: Type %s of offset Constant expression does not match expected i32", src.type()));
                }
                yield (int) src.getValAsLong();
            }
            case RefTypeInstruction.RefFunc e -> throw new RuntimeException(String.format("INIT_ERROR: Type %s of offset Constant expression does not match expected i32", e));
            case RefTypeInstruction.RefNull e -> throw new RuntimeException(String.format("INIT_ERROR: Type %s of offset Constant expression does not match expected i32", e));
            default -> throw new IllegalStateException("Unexpected value: " + expr);
        };
    }

    private void processActiveElementSegments() {
        for (var ds : elementSegments()) {
            if (!(ds instanceof ElementSegment.ActiveElementSegment aes)) {
                continue;
            }
            Table t = tables[aes.tableIndex()];
            if (t.type() != aes.type()) {
                throw new RuntimeException(String.format("INIT_ERROR: Table type %s does not match element type %s", t.type(), aes.type()));
            }
            int offset = getOffsetFromConstExpr(aes.initExpr());
            int n = (aes.isExpression()) ? aes.expressionVector().length : aes.functionIndexVector().length;
            if (aes.isExpression()) {
                // TODO resolve expression vectors
                for (var e : aes.expressionVector()) {
                    System.out.println(e.opCode());
                    switch (e) {
                        case ConstInstruction.IntConst intConst -> {
                        }
                        case GlobalInstruction.GlobalGet globalGet -> {
                        }
                        case RefTypeInstruction.RefFunc refFunc -> {
                        }
                        case RefTypeInstruction.RefNull refNull -> {
                        }
                        case ConstInstruction.DoubleConst doubleConst -> {
                        }
                        case ConstInstruction.FloatConst floatConst -> {
                        }
                        case ConstInstruction.LongConst longConst -> {
                        }
                    }
                }
            } else {
                for (int i = 0; i < n; i++) {
                    t.set(offset+i, functions()[aes.functionIndexVector()[i]]);
                }
            }
            // m.store(offset, ads.data(), 0, ads.data().length);
            // TODO: drop element after copy
        }
    }

    private void processDataSegments() {
        for (var ds : dataSegments()) {
            if (!(ds instanceof DataSegment.ActiveDataSegment ads)) {
                continue;
            }
            Memory m = memories[ads.memoryIndex()];
            int offset = getOffsetFromConstExpr(ads.offset());
            m.store(offset, ads.data(), 0, ads.data().length);
        }
    }

    private Map<String, Object> processExports() {
        Map<String, Object> exportMap = new HashMap<>();
        for (var e : exports()) {
            switch (e.descriptor()) {
                case ExportDescriptor.FunctionDescriptor(int idx) -> {
                    functions[idx] = Function.rename(e.name(), functions[idx]);
                    exportMap.put(e.name(), functions[idx]);
                }
                case ExportDescriptor.GlobalDescriptor(int idx) -> exportMap.put(e.name(), globals()[idx]);
                case ExportDescriptor.MemoryDescriptor(int idx) -> exportMap.put(e.name(), memories()[idx]);
                case ExportDescriptor.TableDescriptor(int idx) -> exportMap.put(e.name(), tables()[idx]);
            }
        }
        return Collections.unmodifiableMap(exportMap);
    }

    public Machine instantiate(Map<String, Map<String, Object>> importMap) {
        return instantiate(importMap, MachineVisitors.NULL_VISITOR);
    }

    public Machine instantiate(Map<String, Map<String, Object>> importMap, MachineVisitor visitor) {
        /*
            https://www.w3.org/TR/wasm-core-2/exec/modules.html#exec-instantiation
            TODO:
            4. Tables - Element segments
         */
        processImports(importMap);
        processGlobals();
        processDataSegments();
        processActiveElementSegments();
        var exportMap = processExports();
        return new Machine(functions(), tables(), globals(), memories(), dataSegments(), elementSegments(), exportMap, startIdx(), visitor);
    }
}
