package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.InstructionDefinitions;

public class NoInstruction extends Instruction {

	public NoInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}
}
