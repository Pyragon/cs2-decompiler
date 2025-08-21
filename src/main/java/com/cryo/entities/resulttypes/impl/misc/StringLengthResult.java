package com.cryo.entities.resulttypes.impl.misc;

import com.cryo.entities.Type;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class StringLengthResult extends ResultType {

	private final ResultType value;

	public StringLengthResult(ResultType value) {
		this.value = value;
	}

	@Override
	public void print(Printer printer) {
		value.print(printer);
		printer.print(".length()");
	}
}
