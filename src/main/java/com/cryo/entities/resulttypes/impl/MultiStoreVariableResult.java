package com.cryo.entities.resulttypes.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class MultiStoreVariableResult extends ResultType {

	private final CS2Script.Variable[] variables;
	private final InstructionDefinitions defs;
	private final ArrayList<ResultType> arguments;

	public MultiStoreVariableResult(CS2Script.Variable[] variables, InstructionDefinitions defs, ArrayList<ResultType> arguments) {
		this.variables = variables;
		this.defs = defs;
		this.arguments = arguments;
	}

	@Override
	public void print(Printer printer) {
		printer.print("[ ");
		for(int i = 0; i < variables.length; i++) {
			printer.print(variables[i].name());
			if(i != variables.length - 1)
				printer.print(", ");
		}
		printer.print(" ] = ");
		printer.print(defs.name()+"(");
		for(int i = 0; i < arguments.size(); i++) {
			arguments.get(i).print(printer);
			if(i != arguments.size() - 1)
				printer.print(", ");
		}
		printer.print(")");
	}
}
