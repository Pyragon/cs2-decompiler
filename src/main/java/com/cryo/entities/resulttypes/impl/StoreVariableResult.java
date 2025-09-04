package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class StoreVariableResult extends ResultType {

	private final String name;
	private final ResultType type;

	public StoreVariableResult(String name, ResultType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public void print(Printer printer) {
		printer.print(name + " = ");
		type.print(printer);
	}
}
