package rrampage.wasp.examples;

import rrampage.wasp.data.Function;
import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.Memory;
import rrampage.wasp.data.Module;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitors;

import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class RiscVEmulator {
    Module module;
    Machine machine;
    final Memory memory;
    int WASM_VECTOR_LEN;
    int ptr;
    boolean isRunning = true;
    ArrayDeque<String> inputs;

    public RiscVEmulator() {
        try {
            byte[] wasmModuleBytes = ProgramUtils.downloadAsBytes("https://raw.githubusercontent.com/rrampage/wart/master/examples/wasm-bindgen/riscv-emu/riscv-emu.wasm");
            System.out.println(STR."Downloaded wasm module of length \{wasmModuleBytes.length} bytes");
            module = new WasmParser(wasmModuleBytes).parseModule();
            System.out.println(STR."Module: \{module.memories().length}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        machine = module.instantiate(createImportMap(), MachineVisitors.NULL_VISITOR);
        memory = (Memory) machine.exports().get("memory");
        machine.invoke("wasmriscv_new");
        ptr = machine.popInt();
        System.out.println(STR."RISCV_INIT ptr:\{ptr}");
    }

    private void throwString(int addr, int len) {
        throw new RuntimeException(ProgramUtils.loadString(memory, addr, len));
    }

    private Map<String,Map<String,Object>> createImportMap() throws RuntimeException {
        try {
            var throwMh = Function.createImportFunction("load", FunctionType.I32_BICONSUME,
                    MethodHandles.lookup().findVirtual(this.getClass(), "throwString", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_BICONSUME)).bindTo(this));
            return Map.of("wbg", Map.of("__wbindgen_throw", throwMh));
        } catch (Exception e) {
            throw new RuntimeException("RISCV_EMU_INIT Method Handle lookup failed");
        }
    }

    private int passArray8ToWasm0(byte[] arr) {
        machine.invoke("__wbindgen_malloc", constOf(arr.length));
        var ptr = machine.popInt();
        System.out.println(STR."MALLOC: ptr:\{ptr} Arr:\{arr.length} bytes");
        memory.buffer().put(ptr, arr);
        WASM_VECTOR_LEN = arr.length;
        byte[] memData = memory.load(ptr, arr.length);
        System.out.println(STR."MALLOC_CHECK: \{Arrays.equals(arr, memData)}");
        return ptr;
    }

    public void setupProgram(byte[] program) {
        var ptr0 = passArray8ToWasm0(program);
        var len0 = WASM_VECTOR_LEN;
        System.out.println(STR."RISCV_SETUP_PROGRAM ptr: \{ptr} ptr0: \{ptr0} len0: \{len0}");
        machine.invoke("wasmriscv_setup_program", constOf(ptr), constOf(ptr0), constOf(len0));
    }

    public void setupFileSystem(byte[] content) {
        var ptr0 = passArray8ToWasm0(content);
        var len0 = WASM_VECTOR_LEN;
        System.out.println(STR."RISCV_SETUP_FILESYSTEM ptr: \{ptr} ptr0: \{ptr0} len0: \{len0}");
        machine.invoke("wasmriscv_setup_filesystem", constOf(ptr), constOf(ptr0), constOf(len0));
    }

    public char getOutput() {
        machine.invoke("wasmriscv_get_output", constOf(ptr));
        return (char) machine.popInt();
    }

    public void runCycles(int numCycles) {
        machine.invoke("wasmriscv_run_cycles", constOf(ptr), constOf(numCycles));
    }

    public void flush() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char data = getOutput();
            if (data == 0) {
                break;
            }
            sb.append(data);
        }
        if (!sb.isEmpty()) {
            System.out.print(sb);
        }
    }

    public void run() {
        while (isRunning) {
            flush();
            runCycles(0x100000);
        }
        /*const runCycles = () => {
            while (this.inputs.length > 0) {
                this.riscv.put_input(this.inputs.shift());
            }
        };
        runCycles();*/
    }

    /*
    pub fn new() -> Self {WasmRiscv { emulator: Emulator::new(Box::new(DefaultTerminal::new()))}}
    pub fn setup_program(&mut self, content: Vec<u8>) { self.emulator.setup_program(content);}
    pub fn load_program_for_symbols(&mut self, content: Vec<u8>) {self.emulator.load_program_for_symbols(content);}
    pub fn setup_filesystem(&mut self, content: Vec<u8>) {self.emulator.setup_filesystem(content);}
    pub fn setup_dtb(&mut self, content: Vec<u8>) {self.emulator.setup_dtb(content);}
    pub fn run(&mut self) {self.emulator.run();}
    // Runs program set by `setup_program()` in `cycles` cycles.
    pub fn run_cycles(&mut self, cycles: u32) {
        for _i in 0..cycles {
			self.emulator.tick();
		}
    }
    // Runs program until breakpoints. Also known as debugger's continue command.
    // If the program doesn't hit any breakpoint in `max_cycles` cycles this method returns `false`. Otherwise `true`.
    pub fn run_until_breakpoints(&mut self, breakpoints: Vec<u64>, max_cycles: u32) -> bool {
		let mut table = HashMap::new();
		for i in 0..breakpoints.len() {
			table.insert(breakpoints[i], true);
		}
		for _i in 0..max_cycles {
			self.emulator.tick();
			let pc = self.emulator.get_cpu().read_pc();
			if table.contains_key(&pc) {
				return true;
			}
		}
		return false;
	}
	// Disassembles an instruction Program Counter points to. Use `get_output()` to get the disassembled strings.
	pub fn disassemble_next_instruction(&mut self) {
		let s = self.emulator.get_mut_cpu().disassemble_next_instruction();
		let bytes = s.as_bytes();
		for i in 0..bytes.len() {
			self.emulator.get_mut_terminal().put_byte(bytes[i]);
		}
	}
	// Loads eight-byte data from memory. Loading can cause an error or trap.
	pub fn load_doubleword(&mut self, address: u64, error: &mut [u8]) -> u64 {
		for i in 0..8 {
			match self.emulator.get_mut_cpu()
				.get_mut_mmu().validate_address(address.wrapping_add(i)) {
				Ok(valid) => {
					if !valid {
						error[0] = 2;
						return 0;
					}
				},
				Err(()) => {
					error[0] = 1;
					return 0;
				}
			}
		}
		match self.emulator.get_mut_cpu().get_mut_mmu().load_doubleword(address) {
			Ok(data) => {
				error[0] = 0;
				data
			},
			Err(_trap) => {
				error[0] = 1;
				0
			}
		}
	}
	pub fn read_register(&self, reg: u8) -> u64 { self.emulator.get_cpu().read_register(reg) as u64 }
	pub fn read_pc(&self) -> u64 { self.emulator.get_cpu().read_pc() }
	pub fn get_output(&mut self) -> u8 { self.emulator.get_mut_terminal().get_output() }
	pub fn put_input(&mut self, data: u8) { self.emulator.get_mut_terminal().put_input(data); }
	pub fn enable_page_cache(&mut self, enabled: bool) { self.emulator.enable_page_cache(enabled); }
	pub fn get_address_of_symbol(&mut self, s: String, error: &mut [u8]) -> u64 {
	    match self.emulator.get_addredd_of_symbol(&s) {
			Some(address) => {
				error[0] = 0;
				address
			},
			None => {
				error[0] = 1;
				0
			}
		}
	}
    */

    /*
    Usage of API:
    const riscv = WasmRiscv.new();
    riscv.setup_program(new Uint8Array(elfBuffer));
    riscv.setup_filesystem(new Uint8Array(fsBuffer));
    const runCycles = () => {
        setTimeout(runCycles, 0);
        riscv.run_cycles(0x100000);
        while (true) {
            const data = riscv.get_output();
            if (data !== 0) {
                // print data
            } else {
                break;
            }
        }
        while (inputs.length > 0) {
            riscv.put_input(inputs.shift());
        }
     };
     runCycles();
     */

    public static void main(String[] args) throws Exception {
        /*
            Import[1]:
             - func[0] sig=5 type[5] (i32, i32) -> nil <wasm_bindgen::__wbindgen_throw::hdcd9e8ee4be63f64> <- wbg.__wbindgen_throw
            Export[22]:
             - memory[0] -> "memory"
             - func[35] <__wbg_wasmriscv_free> -> "__wbg_wasmriscv_free"
             - func[127] <wasmriscv_new> -> "wasmriscv_new"
             - func[222] <wasmriscv_setup_program> -> "wasmriscv_setup_program"
             - func[223] <wasmriscv_load_program_for_symbols> -> "wasmriscv_load_program_for_symbols"
             - func[224] <wasmriscv_setup_filesystem> -> "wasmriscv_setup_filesystem"
             - func[225] <wasmriscv_setup_dtb> -> "wasmriscv_setup_dtb"
             - func[281] <wasmriscv_run> -> "wasmriscv_run"
             - func[240] <wasmriscv_run_cycles> -> "wasmriscv_run_cycles"
             - func[66] <wasmriscv_run_until_breakpoints> -> "wasmriscv_run_until_breakpoints"
             - func[150] <wasmriscv_disassemble_next_instruction> -> "wasmriscv_disassemble_next_instruction"
             - func[97] <wasmriscv_load_doubleword> -> "wasmriscv_load_doubleword"
             - func[226] <wasmriscv_read_register> -> "wasmriscv_read_register"
             - func[227] <wasmriscv_read_pc> -> "wasmriscv_read_pc"
             - func[252] <wasmriscv_get_output> -> "wasmriscv_get_output"
             - func[262] <wasmriscv_put_input> -> "wasmriscv_put_input"
             - func[272] <wasmriscv_enable_page_cache> -> "wasmriscv_enable_page_cache"
             - func[115] <wasmriscv_get_address_of_symbol> -> "wasmriscv_get_address_of_symbol"
             - func[280] <__wbindgen_malloc> -> "__wbindgen_malloc"
             - global[0] -> "__wbindgen_export_1"
             - func[319] <__wbindgen_free> -> "__wbindgen_free"
             - func[299] <__wbindgen_realloc> -> "__wbindgen_realloc"
         */
        /*
            https://github.com/riscv-software-src/opensbi
            Supervisor: https://takahirox.github.io/riscv-rust/resources/linux/opensbi/fw_payload.elf
            Rootfs image: https://takahirox.github.io/riscv-rust/resources/linux/rootfs.img

         */
        long start = System.nanoTime();
        byte[] supervisor = ProgramUtils.downloadAsBytes("https://takahirox.github.io/riscv-rust/resources/linux/opensbi/fw_payload.elf");
        // old BBL loader
        // byte[] supervisor = ProgramUtils.downloadAsBytes("https://takahirox.github.io/riscv-rust/resources/linux/bbl/bbl");
        byte[] rootfs = ProgramUtils.downloadAsBytes("https://takahirox.github.io/riscv-rust/resources/linux/rootfs.img");
        // xv6
        // byte[] supervisor = ProgramUtils.downloadAsBytes("https://takahirox.github.io/riscv-rust/resources/xv6/kernel");
        // FileUtils.writeBinaryFile("./examples/tmp/xv6.elf", supervisor);
        // byte[] rootfs = ProgramUtils.downloadAsBytes("https://takahirox.github.io/riscv-rust/resources/xv6/fs.img");
        System.out.println(STR."Supervisor: \{supervisor.length} bytes\nRootfs: \{rootfs.length} bytes\nTook: \{(System.nanoTime() - start)/1_000_000} ms");
        RiscVEmulator emu = new RiscVEmulator();
        try {
            emu.setupProgram(supervisor);
            System.out.println("Setup supervisor complete!");
            emu.setupFileSystem(rootfs);
            System.out.println("Setup filesystem complete!");
            emu.run();
        } catch (Exception e) {
            System.out.println(STR."RISCV_CRASH: \{e.getMessage()}\nMemory: \{emu.memory.getMemorySize()}");
            throw e;
        }
    }
}
