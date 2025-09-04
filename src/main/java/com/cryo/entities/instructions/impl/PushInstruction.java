package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LiteralResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;


public class PushInstruction extends Instruction {

	public PushInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		Type type;
		switch(defs) {
			case PUSH_INT -> type = Type.INT;
			case PUSH_LONG -> type = Type.LONG;
			case PUSH_STRING -> type = Type.STRING;
			default -> throw new IllegalArgumentException("Invalid instruction definition for PushInstruction: " + defs);
		}
		script.getStack(type).push(new LiteralResult(value, type));
	}
}
