package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.SwitchCase;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class CaseResult extends ResultType {

	private final ArrayList<SwitchCase> switchCases;
	private final ArrayList<ResultType> scope;
	private final boolean isDefault;

	public CaseResult(ArrayList<SwitchCase> switchCases, ArrayList<ResultType> scope) {
		this(switchCases, scope, false);
	}

	public CaseResult(ArrayList<SwitchCase> switchCases, ArrayList<ResultType> scope, boolean isDefault) {
		this.switchCases = switchCases;
		this.scope = scope;
		this.isDefault = isDefault;
	}

	@Override
	public void print(Printer printer) {
		printer.printIndent();
		if(isDefault)
			printer.print("default: {");
		else {
			for(int i = 0; i < switchCases.size(); i++) {
				SwitchCase switchCase = switchCases.get(i);
				printer.print("case "+switchCase.getCaseNum()+":");
				if(i != switchCases.size() - 1) {
					printer.newLine();
					printer.printIndent();
				} else
					printer.print(" {");
			}
		}
		printer.indent();
		printer.newLine();
		for(int i = 0; i < scope.size(); i++) {
			ResultType result = scope.get(i);
			result.print(printer, true);
		}
		if(!(scope.getLast() instanceof ReturnResult)) {
			printer.printIndent();
			printer.print("break;");
			printer.newLine();
		}
		printer.outdent();
		printer.printIndent();
		printer.print("};");
	}
}
