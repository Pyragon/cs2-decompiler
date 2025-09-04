package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class StoreArrayResult extends ResultType {

	private final int index;
	private final ResultType arrayIndex;
	private final ResultType value;

	public StoreArrayResult(int index, ResultType arrayIndex, ResultType value) {
		this.index = index;
		this.arrayIndex = arrayIndex;
		this.value = value;
	}

	@Override
	public void print(Printer printer) {
		printer.print("store_array(");
		arrayIndex.print(printer);
		printer.print(", ");
		value.print(printer);
		printer.print(")");
	}
}
