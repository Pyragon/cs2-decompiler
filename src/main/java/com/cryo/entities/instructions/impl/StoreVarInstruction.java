package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.StoreVarResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class StoreVarInstruction extends Instruction {

	public StoreVarInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		Type type = defs == InstructionDefinitions.STORE_VARC_STRING ? Type.STRING : Type.INT;
		ResultType value = script.getStack(type).pop();
		if (value == null)
			throw new IllegalStateException("Value is null for instruction: " + defs.name());
		int id = (int) this.value;
		if (id < 0)
			throw new IllegalStateException("ID cannot be negative: " + id + " in instruction: " + defs.name());
		if(results == null) results = script.getResults();
		results.add(new StoreVarResult(defs, value, id));
	}
}
