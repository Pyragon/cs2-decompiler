package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class LoadArrayResult extends ResultType {

	private final int index;
	private final ResultType arrayIndex;

	public LoadArrayResult(int index, ResultType arrayIndex) {
		this.index = index;
		this.arrayIndex = arrayIndex;
	}

	@Override
	public void print(Printer printer) {
		printer.print("load_array(");
		arrayIndex.print(printer);
		printer.print(")");
	}
}
