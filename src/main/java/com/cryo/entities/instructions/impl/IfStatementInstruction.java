package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.IfStatementResult;
import com.cryo.utils.Logger;

import java.util.ArrayList;
import java.util.Iterator;

public class IfStatementInstruction extends Instruction {

	private final ArrayList<ResultType> resultTypes;
	private final ArrayList<IfStatement> andInstructions;

	public IfStatementInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
		this.resultTypes = new ArrayList<>();
		this.andInstructions = new ArrayList<>();
	}

	@Override
	public void process(Iterator<Instruction> iterator, ArrayList<ResultType> results) {
		Type type = Type.INT;
		if(defs.name().startsWith("LONG"))
			type = Type.LONG;
		ResultType right = script.getStack(type).pop();
		if(right == null) {
			throw new IllegalStateException("Right operand is null for instruction: " + defs.name());
		}
		ResultType left = script.getStack(type).pop();
		if(left == null) {
			throw new IllegalStateException("Left operand is null for instruction: " + defs.name());
		}
		andInstructions.add(new IfStatement(left, right, defs));
		if(!iterator.hasNext()) {
			throw new IllegalStateException("No next instruction for if instruction: " + defs.name());
		}
		Instruction gotoInstruction = iterator.next();
		if(gotoInstruction.getDefinitions() != InstructionDefinitions.GOTO) {
			throw new IllegalStateException("Next instruction is not a GOTO for if instruction: " + defs.name() + ", found: " + gotoInstruction.getDefinitions().name());
		}
		int length = (int) gotoInstruction.getValue();
		if(length < 0) {
			throw new IllegalStateException("Length for GOTO instruction cannot be negative: " + length + " in instruction: " + defs.name());
		}
		Logger.log(this.getClass(), "GOTO length: " + length + " for instruction: " + defs.name());
		for(int i = 0; i <= length; i++) { //TODO - why does this need <= instead of <
			if(!iterator.hasNext()) {
				throw new IllegalStateException("Not enough instructions to skip for if instruction: " + defs.name());
			}
			Instruction nextInstruction = iterator.next();
			Logger.log(this.getClass(), "Processing: " + nextInstruction.getDefinitions().name() + " in if statement instruction: " + defs.name());
			if(nextInstruction instanceof IfStatementInstruction) {
				Type type2 = Type.INT;
				if(nextInstruction.getDefinitions().name().startsWith("LONG"))
					type2 = Type.LONG;
				Instruction nextGotoInstruction = iterator.next();
				if(nextGotoInstruction.getDefinitions() != InstructionDefinitions.GOTO) {
					throw new IllegalStateException("Next instruction after IfStatement is not a GOTO: " + nextGotoInstruction.getDefinitions().name());
				}
				int nextLength = (int) nextGotoInstruction.getValue();
				if(nextLength != length - (i + 2)) { //account for the fact that we took two instructions off
					throw new IllegalStateException("Next GOTO length does not match expected length: " + nextLength + " != " + (length - i) + " for instruction: " + defs.name());
				}
				ResultType right2 = script.getStack(type2).pop();
				if(right2 == null) {
					throw new IllegalStateException("Right operand is null for next instruction: " + nextGotoInstruction.getDefinitions().name());
				}
				ResultType left2 = script.getStack(type2).pop();
				if(left2 == null) {
					throw new IllegalStateException("Left operand is null for next instruction: " + nextGotoInstruction.getDefinitions().name());
				}
				andInstructions.add(new IfStatement(left2, right2, nextInstruction.getDefinitions()));
				Logger.log(this.getClass(), "Added an AndInstruction: " + nextInstruction.getDefinitions().name() + " with left: " + left2 + " and right: " + right2);
				i += 2;
			} else
				nextInstruction.process(iterator, resultTypes);
		}
		script.getResults().add(new IfStatementResult(defs, andInstructions, resultTypes));
	}

	public record IfStatement(ResultType left, ResultType right, InstructionDefinitions defs) {}
}
