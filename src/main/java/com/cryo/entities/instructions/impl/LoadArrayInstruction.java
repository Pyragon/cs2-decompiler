package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LoadArrayResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class LoadArrayInstruction extends Instruction {

	public LoadArrayInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> resultTypes) {
		int index = (int) this.value;
		ResultType arrayIndex = script.getStack(Type.INT).pop();
		script.getStack(Type.INT).push(new LoadArrayResult(index, arrayIndex));
	}
}
