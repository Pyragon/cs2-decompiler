package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.ReturnResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class ReturnInstruction extends Instruction {

	public ReturnInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		//TODO - add checks to ensure the return type matches the expected type
		//TODO - handle multiple return types
		ResultType type = null;
		if(!script.getStack(Type.INT).empty())
			type = script.getStack(Type.INT).pop();
		else if(!script.getStack(Type.STRING).empty())
			type = script.getStack(Type.STRING).pop();
		else if(!script.getStack(Type.LONG).empty())
			type = script.getStack(Type.LONG).pop();
		if(results == null) results = script.getResults();
		results.add(new ReturnResult(type));
	}
}
