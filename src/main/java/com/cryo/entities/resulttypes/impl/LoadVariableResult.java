package com.cryo.entities.resulttypes.impl;

import com.cryo.CS2Script;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class LoadVariableResult extends ResultType {

	private final CS2Script.Variable variable;

	public LoadVariableResult(CS2Script.Variable variable) {
		super();
		this.variable = variable;
	}

	@Override
	public void print(Printer printer) {
		printer.print(variable.name());
	}

	public CS2Script.Variable getVariable() {
		return variable;
	}
}
