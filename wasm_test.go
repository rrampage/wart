package main

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestNoop(t *testing.T) {
	assrt := assert.New(t)
	// Do nothing
	ins := []Instruction{}
	m := NewMachine(nil, 0)
	m.Execute(ins, nil, 0)
	assrt.Equal(0, m.stackPtr)
}

func TestConst(t *testing.T) {
	assrt := assert.New(t)
	// Just push a const
	ins := []Instruction{
		{"const", []int{1}},
	}
	m := NewMachine(nil, 0)
	m.Execute(ins, nil, 0)
	assrt.Equal(1, m.stackPtr)
}

func TestAdd(t *testing.T) {
	assrt := assert.New(t)
	// Just push a const
	ins := []Instruction{
		{"const", []int{1}},
		{"const", []int{111}},
		{"add", nil},
	}
	m := NewMachine(nil, 0)
	m.Execute(ins, nil, 0)
	assrt.Equal(1, m.stackPtr)
	assrt.Equal(112, m.stack[0])
}

func TestLoadStore(t *testing.T) {
	assrt := assert.New(t)
	xAddr := 0
	yAddr := 4
	xVal := 123
	yVal := 234
	ins := []Instruction{
		{"const", []int{xAddr}},
		{"const", []int{xVal}},
		{"store", nil},
		{"const", []int{yAddr}},
		{"const", []int{yVal}},
		{"store", nil},
		{"const", []int{xAddr}},
		{"load", nil},
		{"const", []int{yAddr}},
		{"load", nil},
		{"sub", nil},
	}
	m := NewMachine(nil, 65536)
	m.Execute(ins, nil, 0)
	assrt.Equal(1, m.stackPtr)
	assrt.Equal(111, m.stack[0])
}

func TestUpdatePositionLoadStore(t *testing.T) {
	// x = x + v*dt
	assrt := assert.New(t)
	m := NewMachine(nil, 65536)
	xAddr := 0
	vAddr := 4
	m.Store(xAddr, IntToBytes(200))
	m.Store(vAddr, IntToBytes(3))
	ins := []Instruction{
		{"const", []int{xAddr}},
		{"const", []int{xAddr}},
		{"load", nil},
		{"const", []int{vAddr}},
		{"load", nil},
		{"const", []int{1}},
		{"mul", nil},
		{"add", nil},
		{"store", nil},
	}
	m.Execute(ins, nil, 0)
	assrt.Equal(0, m.stackPtr)
	assrt.Equal(203, BytesToInt(m.memory[xAddr:xAddr+4]))
}

func TestUpdatePositionFunc(t *testing.T) {
	// def updatePosition(x, v, dt):
	//    return x + v*dt
	assrt := assert.New(t)
	updatePosition := NewFunction(3, true, []Instruction{
		{"local.get", []int{0}}, // x
		{"local.get", []int{1}}, // v
		{"local.get", []int{2}}, // dt
		{"mul", nil},            // v*dt
		{"add", nil},            // x + v*dt
	})
	functions := []*Function{updatePosition}
	m := NewMachine(functions, 65536)
	xAddr := 0
	vAddr := 4
	m.Store(xAddr, IntToBytes(200))
	m.Store(vAddr, IntToBytes(3))
	ins := []Instruction{
		{"const", []int{xAddr}},
		{"const", []int{xAddr}},
		{"load", nil},
		{"const", []int{vAddr}},
		{"load", nil},
		{"const", []int{1}},
		{"call", []int{0}}, // call updatePosition
		{"store", nil},     // Set xAddr to function's return value
	}
	m.Execute(ins, nil, 0)
	assrt.Equal(0, m.stackPtr)
	assrt.Equal(203, BytesToInt(m.memory[xAddr:xAddr+4]))
}
