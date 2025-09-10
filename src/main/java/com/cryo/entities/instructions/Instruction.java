package com.cryo.entities.instructions;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Logger;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class Instruction {

	protected final InstructionDefinitions defs;
	protected final CS2Script script;
	protected final Object value;

	public Instruction(InstructionDefinitions defs, CS2Script script, Object value) {
		this.defs = defs;
		this.script = script;
		this.value = value;
	}

	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		Logger.log(this.getClass(), "Processing instruction: " + defs.name() + " with value: " + value+" HasScope: "+(results != null));
//		Logger.log(this.getClass(), "Current stack: "+
//				"intStack=" + script.getStack(Type.INT).size() +
//				", stringStack=" + script.getStack(Type.STRING).size() +
//				", longStack=" + script.getStack(Type.LONG).size()
//		);
	}

	public InstructionDefinitions getDefinitions() {
		return defs;
	}

	public Object getValue() {
		return value;
	}
}
