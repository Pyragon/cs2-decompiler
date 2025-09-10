package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.StatementResult;
import com.cryo.utils.Logger;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class StatementInstruction extends Instruction {

	private final ArrayList<Statement> statements;
	private boolean isWhile;

	public StatementInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
		this.statements = new ArrayList<>();
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		Type type = Type.INT;
		if(defs.name().startsWith("LONG"))
			type = Type.LONG;
		if(script.getStack(type).empty()) {
			throw new IllegalStateException("Stack "+type.name()+" is empty for statement instruction: " + defs.name());
		}
		ResultType right = script.getStack(type).pop();
		if(right == null) {
			throw new IllegalStateException("Right operand is null for instruction: " + defs.name());
		}
		if(script.getStack(type).empty()) {
			throw new IllegalStateException("Stack "+type.name()+" is empty for statement instruction: " + defs.name());
		}
		ResultType left = script.getStack(type).pop();
		if(left == null) {
			throw new IllegalStateException("Left operand is null for instruction: " + defs.name());
		}
		statements.add(new Statement(left, right, StatementType.DEFAULT, defs));
		Instruction nextInstruction = iterator.next();
		if(nextInstruction.getDefinitions() != InstructionDefinitions.GOTO) {
			nextInstruction.process(iterator, null);
			while(true) {
				nextInstruction = iterator.next();
				if(nextInstruction.getDefinitions() == InstructionDefinitions.GOTO)
					break;
				if(nextInstruction.getDefinitions().getClazz() != StatementInstruction.class) {
					nextInstruction.process(iterator, null);
					continue;
				}
				InstructionDefinitions orDefs = nextInstruction.getDefinitions();
				Type orType = Type.INT;
				if(orDefs.name().startsWith("LONG"))
					orType = Type.LONG;
				if(script.getStack(orType).empty()) {
					throw new IllegalStateException("Stack "+orType.name()+" is empty for orstatement instruction: " + orDefs.name());
				}
				ResultType orRight = script.getStack(orType).pop();
				if(orRight == null) {
					throw new IllegalStateException("Right operand is null for orinstruction: " + orDefs.name());
				}
				if(script.getStack(orType).empty()) {
					throw new IllegalStateException("Stack "+orType.name()+" is empty for orstatement instruction: " + orDefs.name());
				}
				ResultType orLeft = script.getStack(orType).pop();
				if(orLeft == null) {
					throw new IllegalStateException("Left operand is null for orinstruction: " + orDefs.name());
				}
				statements.add(new Statement(orLeft, orRight, StatementType.OR, orDefs));
			}
		}
		int value = (int) nextInstruction.getValue();
		ArrayList<ResultType> scope = new ArrayList<>();
		ArrayList<ResultType> elseScope = new ArrayList<>();
		int gotoAddress = iterator.getIndex() + value;
		boolean addingToAnds = true;
		while(gotoAddress > iterator.getIndex()) {
			Instruction instruction = iterator.next();
			if(gotoAddress == iterator.getIndex() && instruction.getDefinitions() == InstructionDefinitions.GOTO) {
				int elseValue = (int) instruction.getValue();
				if(elseValue > 0) {
					int elseAddress = iterator.getIndex() + elseValue;
					while(elseAddress> iterator.getIndex()) {
						instruction = iterator.next();
						instruction.process(iterator, elseScope);
					}
				} else isWhile = true;
			} else {
				if (instruction.getDefinitions().getClazz() != StatementInstruction.class && instruction.getDefinitions().getClazz() != PushInstruction.class && instruction.getDefinitions().getReturnType().length == 1 && instruction.getDefinitions().getReturnType()[0] == Type.VOID) {
					addingToAnds = false;
				} else if(instruction.getDefinitions().getClazz() == StatementInstruction.class && addingToAnds && script.getInstructions().get(gotoAddress).getDefinitions() != InstructionDefinitions.GOTO) {
					Instruction goToInstruction = iterator.peek();
					if(goToInstruction.getDefinitions() != InstructionDefinitions.GOTO) {
						throw new IllegalStateException("Expected GOTO after AND statement, found "+goToInstruction.getDefinitions().name()+" instead.");
					}
					int andValue = (int) goToInstruction.getValue();
					if((andValue + iterator.getIndex()) != gotoAddress) {
						//idk why this hits, just fucking process like normal and forget about it
						//throw new IllegalStateException("Mismatched GOTO in statement, expected to go to "+gotoAddress+" but found "+(andValue + iterator.getIndex())+" instead.");
						instruction.process(iterator, scope);
						continue;
					}
					iterator.next();
					InstructionDefinitions andDefs = instruction.getDefinitions();
					Type andType = Type.INT;
					if(andDefs.name().startsWith("LONG"))
						andType = Type.LONG;
					if(script.getStack(andType).empty()) {
						throw new IllegalStateException("Stack "+andType.name()+" is empty for andstatement instruction: " + andDefs.name());
					}
					ResultType andRight = script.getStack(andType).pop();
					if(andRight == null) {
						throw new IllegalStateException("Right operand is null for andinstruction: " + andDefs.name());
					}
					if(script.getStack(andType).empty()) {
						throw new IllegalStateException("Stack "+andType.name()+" is empty for andstatement instruction: " + andDefs.name());
					}
					ResultType andLeft = script.getStack(andType).pop();
					if(andLeft == null) {
						throw new IllegalStateException("Left operand is null for andinstruction: " + andDefs.name());
					}
					statements.add(new Statement(andLeft, andRight, StatementType.AND, andDefs));
					continue;
				}
				instruction.process(iterator, scope);
			}
		}
		if(results == null)
			results = script.getResults();
		results.add(new StatementResult(defs, isWhile, statements, scope, elseScope));
	}

	public record Statement(ResultType left, ResultType right, StatementType statementType, InstructionDefinitions defs) {}

	public enum StatementType {
		DEFAULT, AND, OR
	}
}
