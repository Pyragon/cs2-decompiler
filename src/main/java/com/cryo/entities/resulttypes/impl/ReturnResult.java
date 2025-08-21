package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.Type;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class ReturnResult extends ResultType {

	private final ResultType returnType;

	public ReturnResult(ResultType returnType) {
		this.returnType = returnType;
	}

	@Override
	public void print(Printer printer) {
		printer.print("return");
		if(returnType != null) {
			printer.print(" ");
			boolean literalString = false;
			if(returnType instanceof LiteralResult result && result.getType() == Type.STRING)
				literalString = true;
			if(literalString) printer.print("\"");
			returnType.print(printer);
			if(literalString) printer.print("\"");
		}
	}
}
