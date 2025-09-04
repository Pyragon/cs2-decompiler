package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LoadVariableResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class LoadVariableInstruction extends Instruction {

	public LoadVariableInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException("LoadInstruction value must be an Integer representing the variable index.");
		}
		int index = (int) value;
		Type type;
		switch(defs) {
			case LOAD_INT -> type = Type.INT;
			case LOAD_LONG -> type = Type.LONG;
			case LOAD_STRING -> type = Type.STRING;
			default -> throw new IllegalArgumentException("Invalid instruction definition for LoadInstruction: " + defs);
		}
		if(!script.getVariablesOfType(type).containsKey(index)) {
			throw new IllegalArgumentException("Variable with index " + index + " does not exist in the script variables of type " + type + ".");
		}
//		if(!script.getVariables().containsKey(index)) {
//			throw new IllegalArgumentException("Variable with index " + index + " does not exist in the script variables.");
//		}
//		CS2Script.Variable variable = script.getVariables().get(index);
		CS2Script.Variable variable = script.getVariableByType(type, index);
		if(variable.type() != type) {
			throw new IllegalArgumentException("Variable type mismatch: expected " + type + " but found " + variable.type() + " for variable index " + index);
		}
		script.getStack(type).push(new LoadVariableResult(variable.name()));
	}
}
