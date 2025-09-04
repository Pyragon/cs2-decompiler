package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class CallScriptResult extends ResultType {

	private final int scriptId;
	private final ArrayList<ResultType> arguments;

	public CallScriptResult(int scriptId, ArrayList<ResultType> arguments) {
		this.scriptId = scriptId;
		this.arguments = arguments;
	}

	@Override
	public void print(Printer printer) {
		printer.print("script"+scriptId+"(");
		for(int i = 0; i < arguments.size(); i++) {
			arguments.get(i).print(printer);
			if(i < arguments.size() - 1)
				printer.print(", ");
		}
		printer.print(")");
	}
}
