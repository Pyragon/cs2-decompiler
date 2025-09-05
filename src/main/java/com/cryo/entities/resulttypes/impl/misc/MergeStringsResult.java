package com.cryo.entities.resulttypes.impl.misc;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LiteralResult;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class MergeStringsResult extends ResultType {

	private final ArrayList<ResultType> strings;

	public MergeStringsResult(ArrayList<ResultType> strings) {
		this.strings = strings;
	}

	@Override
	public void print(Printer printer) {
		printer.print("`");
		for(ResultType str : strings) {
			if(str instanceof LiteralResult result)
				printer.print((String) result.getValue());
			else {
				printer.print("${");
				str.print(printer);
				printer.print("}");
			}
		}
		printer.print("`");
	}
}
