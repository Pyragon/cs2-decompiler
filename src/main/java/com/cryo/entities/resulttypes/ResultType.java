package com.cryo.entities.resulttypes;

import com.cryo.entities.resulttypes.impl.ReturnResult;
import com.cryo.utils.Logger;
import com.cryo.utils.Printer;

public class ResultType {

	public ResultType() {
	}

	public void print(Printer printer) {

	}

	public void print(Printer printer, boolean outer) {
		if(outer)
			printer.printIndent();
		print(printer);
		printer.print(";");
		printer.newLine();
	}
}
