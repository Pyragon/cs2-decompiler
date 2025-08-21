package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.MathsResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;
import java.util.HashMap;

public class MathsInstruction extends Instruction {

	public MathsInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		ResultType right = script.getStack(Type.INT).pop();
		if(right == null) {
			throw new IllegalStateException("Right operand is null for instruction: " + defs.name());
		}
		ResultType left = script.getStack(Type.INT).pop();
		if(left == null) {
			throw new IllegalStateException("Left operand is null for instruction: " + defs.name());
		}
		String operator = MathsOperators.getOperator(defs);
		if(operator == null) {
			throw new IllegalStateException("No operator found for instruction: " + defs.name());
		}
		script.getStack(Type.INT).push(new MathsResult(defs, operator, left, right));
	}

	enum MathsOperators {
		ADD(InstructionDefinitions.ADD, "+"),
		SUBTRACT(InstructionDefinitions.SUBTRACT, "-"),
		DIVIDE(InstructionDefinitions.DIVIDE, "/"),
		MULTIPLY(InstructionDefinitions.MULTIPLY, "*"),
		BITWISE_OR(InstructionDefinitions.BIT_OR, "|"),
		BITWISE_AND(InstructionDefinitions.BIT_AND, "&"),
		BITWISE_NOT(InstructionDefinitions.BIT_NOT, "~"),
		MODULO(InstructionDefinitions.MODULO, "%"),
		POW(InstructionDefinitions.POW, "^");

		private final InstructionDefinitions defs;
		private final String operator;

		private static HashMap<InstructionDefinitions, String> operators;

		static {
			operators = new HashMap<>();
			for(MathsOperators op : values()) {
				operators.put(op.defs, op.operator);
			}
		}

		MathsOperators(InstructionDefinitions defs, String operator) {
			this.defs = defs;
			this.operator = operator;
		}

		public static String getOperator(InstructionDefinitions defs) {
			return operators.get(defs);
		}
	}
}
