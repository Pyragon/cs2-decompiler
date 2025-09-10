package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.db.ParameterDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LiteralResult;
import com.cryo.entities.resulttypes.impl.ParameterResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class ParameterInstruction extends Instruction {

	public ParameterInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		ResultType paramIdResult = script.getStack(Type.INT).pop();
		if(paramIdResult == null) {
			throw new IllegalStateException("Parameter value is null for instruction: " + defs.name());
		}
		if(!(paramIdResult instanceof LiteralResult literal)) {
			throw new IllegalStateException("Parameter value is not a literal for instruction: " + defs.name());
		}
		int paramId = (int) literal.getValue();
		ResultType struct = null;
		if(defs.readsInt())
			struct = script.getStack(Type.INT).pop();
		boolean isStringParam = ParameterDefinitions.isStringParam(paramId);
		script.getStack(isStringParam ? Type.STRING : Type.INT).push(new ParameterResult(defs, paramId, struct));
	}
}
