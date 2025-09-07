package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.Type;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class ReturnResult extends ResultType {

	private final ArrayList<ResultType> returnValues;

	public ReturnResult(ArrayList<ResultType> returnValues) {
		this.returnValues = returnValues;
	}

	@Override
	public void print(Printer printer) {
		printer.print("return");
		if(returnValues != null) {
			printer.print(" ");
			if(returnValues.size() > 1) printer.print("[ ");
			for(int i = 0; i < returnValues.size(); i++) {
				if(i > 0) printer.print(", ");
				returnValues.get(i).print(printer);
			}
			if(returnValues.size() > 1) printer.print(" ]");
		}
	}
}
