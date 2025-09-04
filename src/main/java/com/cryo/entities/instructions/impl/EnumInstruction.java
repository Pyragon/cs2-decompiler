package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.EnumResult;
import com.cryo.entities.resulttypes.impl.LiteralResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class EnumInstruction extends Instruction {

	public EnumInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		ResultType valueId = script.getStack(Type.INT).pop();
		if (valueId == null) {
			throw new IllegalStateException("Value ID is null for instruction: " + defs.name());
		}
		ResultType enumId = script.getStack(Type.INT).pop();
		if (enumId == null) {
			throw new IllegalStateException("Enum ID is null for instruction: " + defs.name());
		}
		ResultType valueType = script.getStack(Type.INT).pop();
		if (valueType == null) {
			throw new IllegalStateException("Value type is null for instruction: " + defs.name());
		}
		ResultType keyType = script.getStack(Type.INT).pop();
		if (keyType == null) {
			throw new IllegalStateException("Key type is null for instruction: " + defs.name());
		}
		if(!(valueType instanceof LiteralResult valueResult)) {
			throw new IllegalStateException("Value type is not a literal for instruction: " + defs.name() + ", found: " + valueType.getClass().getSimpleName());
		}
		if(!(keyType instanceof LiteralResult keyTypeResult)) {
			throw new IllegalStateException("Key type is not a literal for instruction: " + defs.name() + ", found: " + keyType.getClass().getSimpleName());
		}
		int value = (int) valueResult.getValue();
		Type type = value == 's' ? Type.STRING : Type.INT;
		script.getStack(type).push(new EnumResult(keyTypeResult, valueResult, enumId, valueId));
	}
}
