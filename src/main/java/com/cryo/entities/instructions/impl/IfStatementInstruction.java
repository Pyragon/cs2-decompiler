package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.IfStatementResult;
import com.cryo.utils.Logger;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;

public class IfStatementInstruction extends Instruction {

	private final ArrayList<ResultType> scope;
	private final ArrayList<ResultType> elseScope;
	private final ArrayList<IfStatement> statements;

	public IfStatementInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
		this.scope = new ArrayList<>();
		this.elseScope = new ArrayList<>();
		this.statements = new ArrayList<>();
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		Logger.log(this.getClass(), "Processing...");
		Logger.log(this.getClass(), "Current stack: "+
				"intStack=" + script.getStack(Type.INT).size() +
				", stringStack=" + script.getStack(Type.STRING).size() +
				", longStack=" + script.getStack(Type.LONG).size()
		);
		Type type = Type.INT;
		if(defs.name().startsWith("LONG"))
			type = Type.LONG;
		if(script.getStack(type).empty()) {
			throw new IllegalStateException("Stack "+type.name()+" is empty for if instruction: " + defs.name());
		}
		ResultType right = script.getStack(type).pop();
		if(right == null) {
			throw new IllegalStateException("Right operand is null for instruction: " + defs.name());
		}
		if(script.getStack(type).empty()) {
			throw new IllegalStateException("Stack "+type.name()+" is empty for if instruction: " + defs.name());
		}
		ResultType left = script.getStack(type).pop();
		if(left == null) {
			throw new IllegalStateException("Left operand is null for instruction: " + defs.name());
		}
		statements.add(new IfStatement(left, right, IfStatementType.DEFAULT, defs));
		if(!iterator.hasNext()) {
			throw new IllegalStateException("No next instruction for if instruction: " + defs.name());
		}
		Instruction gotoInstruction = iterator.peek();
		if(gotoInstruction.getDefinitions() != InstructionDefinitions.GOTO) {
			//This is an or statement
			int value = (int) this.value - 2; //We don't want to read the next ifstatement/goto, just the leading up to it.
			Logger.log(this.getClass(), "Value for ifStatement "+defs.name()+" is "+value+".");
			//Value is the # of instructions until our GOTO
			for(int i = 0; i < value; i++) {
				Instruction instruction = iterator.next();
				Logger.log(this.getClass(), "Processing instruction in OR statement: " + instruction.getDefinitions().name() + " for if instruction: " + defs.name());
				instruction.process(iterator, null);
				Logger.log(this.getClass(), "Processed instruction: " + instruction.getDefinitions().name() + " in OR statement for if instruction: " + defs.name());
			}
			Logger.log(this.getClass(), "Finished processing instructions in OR statement.");
			//next instruction should be our ifStatement
			Instruction nextInstruction = iterator.next();
			if(!(nextInstruction instanceof IfStatementInstruction nextIfInstruction)) {
				throw new IllegalStateException("Next instruction is not an IfStatement for if instruction: " + defs.name() + ", found: " + nextInstruction.getDefinitions().name());
			}
			Type type2 = Type.INT;
			if(nextIfInstruction.getDefinitions().name().startsWith("LONG"))
				type2 = Type.LONG;
			ResultType right2 = script.getStack(type2).pop();
			if(right2 == null) {
				throw new IllegalStateException("Right operand is null for next instruction: " + nextInstruction.getDefinitions().name());
			}
			ResultType left2 = script.getStack(type2).pop();
			if(left2 == null) {
				throw new IllegalStateException("Left operand is null for next instruction: " + nextInstruction.getDefinitions().name());
			}
			statements.add(new IfStatement(left2, right2, IfStatementType.OR, nextIfInstruction.getDefinitions()));
			Logger.log(this.getClass(), "Added an OrInstruction: " + nextIfInstruction.getDefinitions().name() + " with left: " + left2 + " and right: " + right2);
			gotoInstruction = iterator.peek();
			if(gotoInstruction.getDefinitions() != InstructionDefinitions.GOTO) {
				throw new IllegalStateException("Next instruction is not a GOTO at the end of OR for if instruction: " + defs.name() + ", found: " + gotoInstruction.getDefinitions().name());
			}
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
			if(i == length && nextInstruction.getDefinitions() == InstructionDefinitions.GOTO) {
				int elseLength = (int) nextInstruction.getValue();
				Logger.log(this.getClass(), "Else length: " + elseLength + " for instruction: " + defs.name());
				for(int j = 0; j <= elseLength; j++) {
					if(!iterator.hasNext()) {
						throw new IllegalStateException("Not enough instructions to skip for else statement in if instruction: " + defs.name());
					}
					Instruction elseInstruction = iterator.next();
					Logger.log(this.getClass(), "Processing instruction in else statement: " + elseInstruction.getDefinitions().name() + " for if instruction: " + defs.name());
					elseInstruction.process(iterator, elseScope);
					Logger.log(this.getClass(), "Processed instruction: " + elseInstruction.getDefinitions().name() + " in else statement for if instruction: " + defs.name());
				}
				continue;
			}
			Logger.log(this.getClass(), "Processing: " + nextInstruction.getDefinitions().name() + " in if statement instruction: " + defs.name());
			nextInstruction.process(iterator, scope);
			Logger.log(this.getClass(), "Processed instruction: " + nextInstruction.getDefinitions().name());Logger.log(this.getClass(), "Current stack: "+
					"intStack=" + script.getStack(Type.INT).size() +
					", stringStack=" + script.getStack(Type.STRING).size() +
					", longStack=" + script.getStack(Type.LONG).size()
			);
//			if(nextInstruction instanceof IfStatementInstruction) {
//				Type type2 = Type.INT;
//				if(nextInstruction.getDefinitions().name().startsWith("LONG"))
//					type2 = Type.LONG;
//				Instruction nextGotoInstruction = iterator.peek();
//				ResultType right2 = script.getStack(type2).pop();
//				if(right2 == null) {
//					throw new IllegalStateException("Right operand is null for next instruction: " + nextGotoInstruction.getDefinitions().name());
//				}
//				ResultType left2 = script.getStack(type2).pop();
//				if(left2 == null) {
//					throw new IllegalStateException("Left operand is null for next instruction: " + nextGotoInstruction.getDefinitions().name());
//				}
//				Logger.log(this.getClass(), "Next instruction is an IfStatement within an IfStatement: " + nextInstruction.getDefinitions().name() + " with left: " + left2 + " and right: " + right2);
//				//This is an OrStatement
//				if(nextGotoInstruction.getDefinitions() != InstructionDefinitions.GOTO) {
//					throw new IllegalStateException("Next instruction is not a GOTO for OR statement 2 in if instruction: " + defs.name() + ", found: " + nextGotoInstruction.getDefinitions().name());
//				}
//				int nextLength = (int) nextGotoInstruction.getValue();
//				Logger.log(this.getClass(), "Next length within nested IfStatement: "+nextLength);
//				if(nextLength != length - (i + 2)) { //account for the fact that we took two instructions off
//					nextInstruction = iterator.next();
//					Logger.log(this.getClass(), "Processing next instruction: "+nextInstruction.getDefinitions().name()+" within IfStatement");
//					nextInstruction.process(iterator, resultTypes);
//					Logger.log(this.getClass(), "Processed instruction: " + nextInstruction.getDefinitions().name());Logger.log(this.getClass(), "Current stack: "+
//							"intStack=" + script.getStack(Type.INT).size() +
//							", stringStack=" + script.getStack(Type.STRING).size() +
//							", longStack=" + script.getStack(Type.LONG).size()
//					);
//					continue;
//				}
//				statements.add(new IfStatement(left2, right2, IfStatementType.AND, nextInstruction.getDefinitions()));
//				Logger.log(this.getClass(), "Added an AndInstruction: " + nextInstruction.getDefinitions().name() + " with left: " + left2 + " and right: " + right2);
//				i += 2;
//			} else {
//				//if last instruction
//				if(i == length) {
//					Logger.log(this.getClass(), "Last instruction in if statement: " + nextInstruction.getDefinitions().name());
//					if(nextInstruction.getDefinitions() == InstructionDefinitions.GOTO) {
//						Logger.log(this.getClass(), "Found an else statement.");
//					}
//				}
//				nextInstruction.process(iterator, resultTypes);
//				Logger.log(this.getClass(), "Processed instruction: " + nextInstruction.getDefinitions().name());Logger.log(this.getClass(), "Current stack: "+
//						"intStack=" + script.getStack(Type.INT).size() +
//						", stringStack=" + script.getStack(Type.STRING).size() +
//						", longStack=" + script.getStack(Type.LONG).size()
//				);
//			}
		}
		script.getResults().add(new IfStatementResult(defs, statements, scope, elseScope));
	}

	public record IfStatement(ResultType left, ResultType right, IfStatementType statementType, InstructionDefinitions defs) {}

	public enum IfStatementType {
		DEFAULT, AND, OR
	}
}
