package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.misc.AppendResult;
import com.cryo.entities.resulttypes.impl.misc.StringLengthResult;
import com.cryo.entities.resulttypes.impl.misc.ToStringResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class CustomInstruction extends Instruction {

	public CustomInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		switch(defs) {
			case TO_STRING -> {
				ResultType toConvert = script.getStack(Type.INT).pop();
				if(toConvert == null) {
					throw new IllegalStateException("Value to convert to string is null for instruction: " + defs.name());
				}
				script.getStack(Type.STRING).push(new ToStringResult(toConvert));
			}
			case STRING_LENGTH -> {
				ResultType str = script.getStack(Type.STRING).pop();
				if(str == null) {
					throw new IllegalStateException("String value is null for instruction: " + defs.name());
				}
				script.getStack(Type.INT).push(new StringLengthResult(str));
			}
			case APPEND -> {
				ResultType toAppend = script.getStack(Type.STRING).pop();
				if(toAppend == null) {
					throw new IllegalStateException("String value is null for instruction: " + defs.name());
				}
				ResultType value = script.getStack(Type.STRING).pop();
				if(value == null) {
					throw new IllegalStateException("Value to append is null for instruction: " + defs.name());
				}
				script.getStack(Type.STRING).push(new AppendResult(value, toAppend));
			}
			case LOWER_STRING -> {
				ResultType toLower = script.getStack(Type.STRING).pop();
				if(toLower == null) {
					throw new IllegalStateException("String value is null for instruction: " + defs.name());
				}
				script.getStack(Type.STRING).push(new ToLowerCaseResult(toLower));
			}
		}
	}
}
