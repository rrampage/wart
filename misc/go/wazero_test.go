package _go

import (
	"context"
	"fmt"
	"github.com/stretchr/testify/assert"
	"github.com/tetratelabs/wazero"
	"log"
	"os"
	"testing"
)

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
