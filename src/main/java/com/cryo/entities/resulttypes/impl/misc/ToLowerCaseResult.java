package com.cryo.entities.resulttypes.impl.misc;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class ToLowerCaseResult extends ResultType {

	private final ResultType toLower;

	public ToLowerCaseResult(ResultType toLower) {
		this.toLower = toLower;
	}

	@Override
	public void print(Printer printer) {
		toLower.print(printer);
		printer.print(".toLowerCase()");
	}
}
