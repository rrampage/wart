package _go

import (
	"context"
	"fmt"
	"github.com/stretchr/testify/assert"
	"github.com/tetratelabs/wazero"
	"github.com/tetratelabs/wazero/api"
	"io"
	"log"
	"net/http"
	"os"
	"testing"
)

type RisvEmu struct {
	Ptr     uint32
	Arglen  int32
	Machine wazero.Runtime
	Ctx     context.Context
	Module  api.Module
}

func (r *RisvEmu) Close() {
	err := r.Machine.Close(r.Ctx)
	if err != nil {
		return
	}
}

func (r *RisvEmu) SetupProgram(setup []byte) {
	ptr0 := r.LoadByteArray(setup)
	len0 := r.Arglen
	log.Printf("Malloc Ptr: %d Len %d\n", ptr0, len0)
	setupProgram := r.Module.ExportedFunction("wasmriscv_setup_program")
	_, err := setupProgram.Call(r.Ctx, uint64(r.Ptr), uint64(ptr0), uint64(len0))
	if err != nil {
		log.Panicln(err)
	}
	log.Printf("Ptr: %d\n", r.Ptr)
}

func (r *RisvEmu) SetupFileSystem(content []byte) {
	ptr0 := r.LoadByteArray(content)
	len0 := r.Arglen
	log.Printf("Malloc Ptr: %d Len %d\n", ptr0, len0)
	setupProgram := r.Module.ExportedFunction("wasmriscv_setup_filesystem")
	_, err := setupProgram.Call(r.Ctx, uint64(r.Ptr), uint64(ptr0), uint64(len0))
	if err != nil {
		log.Panicln(err)
	}
	log.Printf("Ptr: %d\n", r.Ptr)
}

func (r *RisvEmu) Malloc(n int) uint32 {
	malloc := r.Module.ExportedFunction("__wbindgen_malloc")
	call, err := malloc.Call(r.Ctx, uint64(n))
	if err != nil {
		log.Panicln(err)
	}
	return uint32(call[0])
}

func (r *RisvEmu) LoadByteArray(arr []byte) uint32 {
	n := len(arr)
	ptr0 := r.Malloc(n)
	memory := r.Module.ExportedMemory("memory")
	memory.Write(ptr0, arr)
	r.Arglen = int32(n)
	return ptr0
}

func (r *RisvEmu) GetOutput() rune {
	call, err := r.Module.ExportedFunction("wasmriscv_get_output").Call(r.Ctx, uint64(r.Ptr))
	if err != nil {
		log.Panicln(err)
	}
	return rune(call[0])
}

func (r *RisvEmu) Flush() {
	var runes = make([]rune, 0, 128)
	for {
		data := r.GetOutput()
		if data == 0 {
			break
		}
		runes = append(runes, data)
	}
	if len(runes) > 0 {
		log.Print(string(runes))
	}
}

func (r *RisvEmu) RunCycles(numCycles uint32) {
	_, err := r.Module.ExportedFunction("wasmriscv_run_cycles").Call(r.Ctx, uint64(r.Ptr), uint64(numCycles))
	if err != nil {
		log.Panicln(err)
	}
}

func (r *RisvEmu) Run() {
	for {
		r.Flush()
		r.RunCycles(0x100000)
	}
}

func NewRiscvEmu() *RisvEmu {
	ctx := context.Background()
	r := wazero.NewRuntimeWithConfig(ctx, wazero.NewRuntimeConfigInterpreter())
	b, err := os.ReadFile("../../examples/wasm-bindgen/riscv-emu/riscv-emu.wasm")
	if err != nil {
		panic("File read failed")
	}
	_, err = r.NewHostModuleBuilder("wbg").
		NewFunctionBuilder().
		WithFunc(func(addr int32, l int32) {
			panic(fmt.Sprintf("Error: %d %d", addr, l))
		}).
		Export("__wbindgen_throw").
		Instantiate(ctx)
	if err != nil {
		log.Panicln(err)
	}
	mod, err := r.Instantiate(ctx, b)
	if err != nil {
		log.Panicln(err)
	}
	var ptr uint32 = 0
	res, err := mod.ExportedFunction("wasmriscv_new").Call(ctx)
	if err != nil {
		log.Panicln(err)
	}
	ptr = uint32(res[0])
	return &RisvEmu{
		Ptr:     ptr,
		Arglen:  0,
		Machine: r,
		Ctx:     ctx,
		Module:  mod,
	}
}

func TestInterpreter(t *testing.T) {
	assrt := assert.New(t)
	// Do nothing
	ctx := context.Background()
	r := wazero.NewRuntimeWithConfig(ctx, wazero.NewRuntimeConfigInterpreter())
	defer r.Close(ctx)
	b, err := os.ReadFile("../../examples/gol/game_of_life.wasm")
	if err != nil {
		assrt.Fail("File read failed")
	}

	mod, err := r.Instantiate(ctx, b)
	if err != nil {
		log.Panicf("failed to instantiate module: %v", err)
	}
	fmt.Printf("%v\n", mod.ExportedFunctionDefinitions())
}

func downloadFile(url string) []byte {
	resp, err := http.Get(url)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	data, err := io.ReadAll(resp.Body)
	if err != nil {
		panic(err)
	}
	return data
}

func TestLinux(t *testing.T) {
	assrt := assert.New(t)
	supervisor := downloadFile("https://takahirox.github.io/riscv-rust/resources/linux/opensbi/fw_payload.elf")
	log.Printf("Downloaded supervisor: %d\n", len(supervisor))
	filesystem := downloadFile("https://takahirox.github.io/riscv-rust/resources/linux/rootfs.img")
	log.Printf("Downloaded filesystem: %d\n", len(filesystem))
	risvEmu := NewRiscvEmu()
	defer risvEmu.Close()
	assrt.NotEqual(0, risvEmu.Ptr)
	log.Printf("Ptr: %d\n", risvEmu.Ptr)
	risvEmu.SetupProgram(supervisor)
	log.Println("Supervisor setup complete")
	risvEmu.SetupFileSystem(filesystem)
	log.Println("Filesystem setup complete")
	risvEmu.Run()
}
