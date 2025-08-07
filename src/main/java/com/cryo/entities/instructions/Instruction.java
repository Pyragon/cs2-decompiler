package com.cryo.entities.instructions;

public class Instruction {

	private final InstructionDefinitions defs;
	private final Object value;

	public Instruction(InstructionDefinitions defs, Object value) {
		this.defs = defs;
		this.value = value;
	}

	public InstructionDefinitions getDefinitions() {
		return defs;
	}

	public Object getValue() {
		return value;
	}
}
