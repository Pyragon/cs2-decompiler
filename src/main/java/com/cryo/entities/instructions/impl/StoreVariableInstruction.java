package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.StoreVariableResult;

public class StoreVariableInstruction extends Instruction {

	public StoreVariableInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process() {
		if(!(value instanceof Integer)) {
			throw new IllegalArgumentException("StoreVariableInstruction value must be an Integer representing the variable index.");
		}
		int index = (int) value;
		if(!script.getVariables().containsKey(index)) {
			throw new IllegalArgumentException("Variable with index " + index + " does not exist in the script variables.");
		}
		CS2Script.Variable variable = script.getVariables().get(index);
		Type type;
		switch(defs) {
			case STORE_INT -> type = Type.INT;
			case STORE_LONG -> type = Type.LONG;
			case STORE_STRING -> type = Type.STRING;
			default -> throw new IllegalArgumentException("Invalid instruction definition for StoreVariableInstruction: " + defs);
		}
		if(variable.type() != type) {
			throw new IllegalArgumentException("Variable type mismatch: expected " + type + " but found " + variable.type() + " for variable index " + index);
		}
		if(script.getStack(type).isEmpty()) {
			throw new IllegalStateException("Stack is empty for type " + type + " when trying to store variable at index " + index);
		}
		ResultType resultType = script.getStack(type).pop();
		script.getResults().add(new StoreVariableResult(variable.name(), resultType));
	}
}
