package com.cryo.entities.resulttypes.impl.misc;

import com.cryo.entities.resulttypes.ResultType;

public class ToStringResult extends ResultType {

	private final ResultType value;

	public ToStringResult(ResultType value) {
		this.value = value;
	}

	@Override
	public void print(com.cryo.utils.Printer printer) {
		value.print(printer);
		printer.print(".toString()");
	}
}
