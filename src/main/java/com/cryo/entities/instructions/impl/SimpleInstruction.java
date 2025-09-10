package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.MultiStoreVariableResult;
import com.cryo.entities.resulttypes.impl.SimpleResult;
import com.cryo.utils.Logger;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;
import java.util.Collections;

public class SimpleInstruction extends Instruction {

	public SimpleInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		ArrayList<ResultType> arguments = new ArrayList<>();
		if(defs.getArguments() != null) {
			for (int i = 0; i < getDefinitions().getArguments().length; i++) {
				String argumentType = getDefinitions().getArguments()[i];
				Type type = Type.fromString(argumentType);
				ResultType resultType = script.getStack(type).pop();
				arguments.add(resultType);
			}
		}
		Collections.reverse(arguments);
		if(results == null) results = script.getResults();
		if(defs.getReturnType().length == 1) {
			if(defs.getReturnType()[0] == Type.VOID)
				results.add(new SimpleResult(defs, arguments));
			else
				script.getStack(defs.getReturnType()[0]).push(new SimpleResult(defs, arguments));
		} else {
			ArrayList<CS2Script.Variable> variables = new ArrayList<>();
			for(int i = 0; i < defs.getReturnType().length; i++) {
				Instruction nextInstruction = iterator.next();
				if(!(nextInstruction instanceof StoreVariableInstruction)) {
					throw new IllegalStateException("Expected StoreVariableInstruction after multi-return instruction, but got: " + nextInstruction.getDefinitions().name());
				}
				int index = (int) nextInstruction.getValue();
				Type type;
				switch(nextInstruction.getDefinitions()) {
					case STORE_INT -> type = Type.INT;
					case STORE_LONG -> type = Type.LONG;
					case STORE_STRING -> type = Type.STRING;
					default -> throw new IllegalArgumentException("Invalid instruction definition for StoreVariableInstruction: " + defs);
				}
				if(!script.getVariablesOfType(type).containsKey(index)) {
					throw new IllegalArgumentException("Variable with index " + index + " and type " + type + " does not exist in the script variables.");
				}
				CS2Script.Variable variable = script.getVariableByType(type, index);
				if(variable.type() != type) {
					throw new IllegalArgumentException("Variable type mismatch: expected " + type + " but found " + variable.type() + " for variable index " + index);
				}
				variables.add(variable);
			}
			Collections.reverse(variables);
			if(results == null) results = script.getResults();
			results.add(new MultiStoreVariableResult(variables.toArray(CS2Script.Variable[]::new), defs.name(), arguments));
		}
	}
}
