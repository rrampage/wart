package rrampage.wasp.parser.types;

import rrampage.wasp.data.ValueType;
import rrampage.wasp.instructions.Instruction;

public record Code(int size, ValueType.NumType[] locals, Instruction[] code) {}
