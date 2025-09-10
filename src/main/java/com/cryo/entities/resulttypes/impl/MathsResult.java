package com.cryo.entities.resulttypes.impl;

import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class MathsResult extends ResultType {
	private final String operator;
	private final ResultType left;
	private final ResultType right;

	public MathsResult(InstructionDefinitions defs, String operator, ResultType left, ResultType right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	@Override
	public void print(Printer printer) {
		printer.print("(");
		left.print(printer);
		printer.print(" " + operator + " ");
		right.print(printer);
		printer.print(")");
	}

	public ResultType getLeft() {
		return left;
	}

	public ResultType getRight() {
		return right;
	}

	public String getOperator() {
		return operator;
	}
}
