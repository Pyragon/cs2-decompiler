package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class SwitchResult extends ResultType {

	private final ResultType switchValue;
	private final ArrayList<CaseResult> caseResults;
	private final CaseResult defaultCase;

	public SwitchResult(ResultType switchValue, ArrayList<CaseResult> caseResults, CaseResult defaultCase) {
		this.switchValue = switchValue;
		this.caseResults = caseResults;
		this.defaultCase = defaultCase;
	}

	@Override
	public void print(Printer printer) {
		printer.print("switch (");
		switchValue.print(printer);
		printer.print(") {");
		printer.indent();
		printer.newLine();

		for(int i = 0; i < caseResults.size(); i++) {
			CaseResult caseResult = caseResults.get(i);
			caseResult.print(printer);
			if(i < caseResults.size() - 1) {
				printer.newLine();
			}
		}

		if(defaultCase != null) {
			printer.newLine();
			defaultCase.print(printer);
		}

		printer.newLine();
		printer.outdent();
		printer.printIndent();
		printer.print("}");
	}
}
