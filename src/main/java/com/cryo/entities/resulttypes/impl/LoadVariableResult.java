package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.resulttypes.ResultType;

public class LoadVariableResult extends ResultType {

	private final String name;

	public LoadVariableResult(String name) {
		super();
		this.name = name;
	}

	@Override
	public void print(com.cryo.utils.Printer printer) {
		printer.print(name);
	}
}
