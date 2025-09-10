package com.cryo.entities.resulttypes.impl;

import com.cryo.CS2Script;
import com.cryo.entities.Type;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class StoreVariableResult extends ResultType {

	private final CS2Script.Variable variable;
	private final ResultType type;

	public StoreVariableResult(CS2Script.Variable variable, ResultType type) {
		this.variable = variable;
		this.type = type;
	}

	@Override
	public void print(Printer printer) {
		if(type instanceof MathsResult mathsResult) {
			if(mathsResult.getLeft() instanceof LoadVariableResult loadVariableResult) {
				if(loadVariableResult.getVariable() == variable) {
					//TODO - add more of course, but lets just make sure ++ works
					if(mathsResult.getOperator().equals("+")) {
						if(mathsResult.getRight() instanceof LiteralResult literal) {
							if((int) literal.getValue() == 1)
								printer.print(variable.name()+"++");
							else
								printer.print(variable.name()+" += "+literal.getValue());
						} else {
							printer.print(variable.name()+" += ");
							mathsResult.getRight().print(printer);
						}
						return;
					}
				}
			}
		}
		if(!variable.isAssigned())
			printer.print("var ");
		variable.assign();
		printer.print(variable.name() + " = ");
		type.print(printer);
	}
}
