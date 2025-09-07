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
		super.process(iterator, results);
		if(results == null) results = script.getResults();
		Type[] returnTypes = script.getDefs().getReturnType();
		if(returnTypes == null || returnTypes.length == 0 || returnTypes[0] == Type.VOID) {
			results.add(new ReturnResult(null));
			return;
		}
		ArrayList<ResultType> returnValues = new ArrayList<>();
		for(int i = 0; i < returnTypes.length; i++)
			returnValues.add(script.getStack(returnTypes[i]).pop());
		results.add(new ReturnResult(returnValues));
	}
}
