package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class WhileInstruction extends Instruction {

	public WhileInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> resultTypes) {

	}
}
