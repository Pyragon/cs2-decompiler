package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class SimpleResult extends ResultType {

	private final InstructionDefinitions defs;
	private final ArrayList<ResultType> arguments;

	public SimpleResult(InstructionDefinitions defs, ArrayList<ResultType> arguments) {
		this.defs = defs;
		this.arguments = arguments;
	}

	@Override
	public void print(Printer printer) {
		printer.print(defs.name()+"(");
		for(int i = 0; i < arguments.size(); i++) {
			arguments.get(i).print(printer);
			if(i != arguments.size() - 1)
				printer.print(", ");
		}
		printer.print(")");
	}
}
