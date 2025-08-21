package com.cryo.entities.resulttypes.impl.misc;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LiteralResult;
import com.cryo.utils.Printer;

public class AppendResult extends ResultType {

	private final ResultType value;
	private final ResultType toAppend;

	public AppendResult(ResultType value, ResultType toAppend) {
		this.value = value;
		this.toAppend = toAppend;
	}

	@Override
	public void print(Printer printer) {
		value.print(printer);
		printer.print(" + ");
		boolean literal = toAppend instanceof LiteralResult;
		if(literal) printer.print("\"");
		toAppend.print(printer);
		if(literal) printer.print("\"");

	}
}
