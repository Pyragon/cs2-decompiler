package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.SimpleResult;

import java.util.ArrayList;
import java.util.Iterator;

public class SimpleInstruction extends Instruction {

	public SimpleInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(Iterator<Instruction> iterator, ArrayList<ResultType> results) {
		ArrayList<ResultType> arguments = new ArrayList<>();
		if(defs.getArguments() != null) {
			for (int i = 0; i < getDefinitions().getArguments().length; i++) {
				String argumentType = getDefinitions().getArguments()[i];
				Type type = Type.fromString(argumentType);
				ResultType resultType = script.getStack(type).pop();
				arguments.add(resultType);
			}
		}
		if(results == null) results = script.getResults();
		if(defs.getReturnType() == null)
			results.add(new SimpleResult(defs, arguments));
		else if(defs.getReturnType().contains(",")) {
			//TODO - multiple return types
			throw new UnsupportedOperationException("Multiple return types are not supported yet for instruction: " + defs.name());
		} else {
			Type type = Type.fromString(defs.getReturnType());
			script.getStack(type).push(new SimpleResult(defs, arguments));
		}
	}
}
