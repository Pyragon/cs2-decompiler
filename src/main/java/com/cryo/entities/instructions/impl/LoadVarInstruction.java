package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LoadVarResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class LoadVarInstruction extends Instruction {

	public LoadVarInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		String name = defs.name().toLowerCase();
		Type type = name.contains("long") ? Type.LONG : name.contains("string") ? Type.STRING : Type.INT;
		script.getStack(type).push(new LoadVarResult(defs, (int) value));
	}
}
