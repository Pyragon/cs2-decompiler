package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LoadVariableResult;

import java.util.ArrayList;
import java.util.Iterator;

public class LoadVariableInstruction extends Instruction {

	public LoadVariableInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(Iterator<Instruction> iterator, ArrayList<ResultType> results) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException("LoadInstruction value must be an Integer representing the variable index.");
		}
		int index = (int) value;
		if(!script.getVariables().containsKey(index)) {
			throw new IllegalArgumentException("Variable with index " + index + " does not exist in the script variables.");
		}
		CS2Script.Variable variable = script.getVariables().get(index);
		Type type;
		switch(defs) {
			case LOAD_INT -> type = Type.INT;
			case LOAD_LONG -> type = Type.LONG;
			case LOAD_STRING -> type = Type.STRING;
			default -> throw new IllegalArgumentException("Invalid instruction definition for LoadInstruction: " + defs);
		}
		if(variable.type() != type) {
			throw new IllegalArgumentException("Variable type mismatch: expected " + type + " but found " + variable.type() + " for variable index " + index);
		}
		script.getStack(type).push(new LoadVariableResult(variable.name()));
	}
}
