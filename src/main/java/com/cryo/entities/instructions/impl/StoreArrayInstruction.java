package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.StoreArrayResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class StoreArrayInstruction extends Instruction {

	public StoreArrayInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		int index = (int) this.value;
		ResultType value = script.getStack(Type.INT).pop();
		ResultType arrayIndex = script.getStack(Type.INT).pop();
		if(results == null)
			results = script.getResults();
		results.add(new StoreArrayResult(index, arrayIndex, value));
	}
}
