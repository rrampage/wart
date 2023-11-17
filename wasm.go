package main

import (
	"encoding/binary"
	"fmt"
)

type Instruction struct {
	Op   string
	Args []int
}

type Function struct {
	NParams int
	Returns bool
	Code    []Instruction
}

func IntToBytes(val int) []byte {
	buf := make([]byte, binary.MaxVarintLen32)
	binary.PutVarint(buf, int64(val))
	return buf
}

func BytesToInt(buf []byte) int {
	v64, n := binary.Varint(buf)
	if n > 4 {
		panic(fmt.Sprintf("Too many bytes!"))
	}
	return int(v64)
}

func NewFunction(nparams int, returns bool, code []Instruction) *Function {
	return &Function{
		NParams: nparams,
		Returns: returns,
		Code:    code,
	}
}

type Machine struct {
	functions []*Function
	memory    []byte
	stack     []int // Assuming int for now
	stackPtr  int
}

func NewMachine(functions []*Function, memSize int) *Machine {
	s := make([]int, 0, 1024)
	m := make([]byte, memSize)
	return &Machine{
		stack:     s,
		memory:    m,
		functions: functions,
	}
}

func (m *Machine) Push(val int) {
	l := len(m.stack)
	if l > m.stackPtr {
		m.stack[m.stackPtr] = val
	} else {
		m.stack = append(m.stack, val)
	}
	m.stackPtr += 1
}

func (m *Machine) Pop() int {
	if m.stackPtr <= 0 {
		panic("Stack underflow")
	}
	pop := m.stack[m.stackPtr-1]
	m.stackPtr = m.stackPtr - 1
	return pop
}

func (m *Machine) Load(addr int, offset int) []byte {
	// Original: struct.unpack('<d', self.mem[addr:addr+8])[0]
	return m.memory[addr : addr+offset]
}

func (m *Machine) Store(addr int, data []byte) {
	// Original: self.mem[addr:addr+8] = struct.pack('<d', val)
	for i, b := range data {
		m.memory[addr+i] = b
	}
}

func (m *Machine) Call(fun *Function, args []int) int {
	m.Execute(fun.Code, args, 0)
	if fun.Returns {
		retVal := m.Pop()
		return retVal
	}
	return 0
}

func (m *Machine) exec(ins Instruction, locals []int, level int) int {
	switch ins.Op {
	case "const":
		// TODO: length check
		m.Push(ins.Args[0])
	case "add":
		left := m.Pop()
		right := m.Pop()
		res := left + right
		fmt.Printf("Op: %s, Eval: %d + %d, Res: %d\n", ins.Op, left, right, res)
		m.Push(res)
	case "sub":
		left := m.Pop()
		right := m.Pop()
		res := left - right
		fmt.Printf("Op: %s, Eval: %d - %d, Res: %d\n", ins.Op, left, right, res)
		m.Push(res)
	case "mul":
		left := m.Pop()
		right := m.Pop()
		res := left * right
		fmt.Printf("Op: %s, Eval: %d * %d, Res: %d\n", ins.Op, left, right, res)
		m.Push(res)
	case "div":
		left := m.Pop()
		right := m.Pop()
		res := left / right
		fmt.Printf("Op: %s, Eval: %d / %d, Res: %d\n", ins.Op, left, right, res)
		m.Push(res)
	case "store":
		// store val @addr
		// Just storing int for now
		val := m.Pop()
		addr := m.Pop()
		m.Store(addr, IntToBytes(val))
	case "load":
		addr := m.Pop()
		loadedBytes := m.Load(addr, 4)
		val := BytesToInt(loadedBytes)
		m.Push(val)
	case "local.get":
		m.Push(locals[ins.Args[0]])
	case "local.set":
		val := m.Pop()
		locals[ins.Args[0]] = val
	case "call":
		fun := m.functions[ins.Args[0]]
		fargs := make([]int, fun.NParams)
		for i, _ := range fargs {
			// We need to add in reverse order as Pop is LIFO
			fargs[fun.NParams-1-i] = m.Pop()
		}
		res := m.Call(fun, fargs)
		fmt.Printf("Op: %s, Args: %v, Res: %d\n", ins.Op, fargs, res)
		if fun.Returns {
			m.Push(res)
		}
	case "br":
		lvl := ins.Args[0]
		return lvl
	case "br_if":
		if m.Pop() == 1 {
			lvl := ins.Args[0]
			return lvl
		}
	case "block":
		// TODO
		lvl := m.Execute([]Instruction{ins}, locals, level)
		if lvl > 0 {
			lvl -= 1
			return lvl
		}
	case "loop":
		// TODO
		return -1
	default:
		panic(fmt.Sprintf("Invalid instruction: %s args: %v", ins.Op, ins.Args))
	}
	return level
}

func (m *Machine) Execute(instructions []Instruction, locals []int, level int) int {
	for _, ins := range instructions {
		fmt.Printf("Ins: %s, Args: %v, Stack: %v\n", ins.Op, ins.Args, m.stack[:m.stackPtr])
		level = m.exec(ins, locals, level)
	}
	return level
}

func main() {
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
	fmt.Printf("%v\n", m.stack[:m.stackPtr])
}
