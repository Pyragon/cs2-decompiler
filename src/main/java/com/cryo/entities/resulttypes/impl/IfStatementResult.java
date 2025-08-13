package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.entities.instructions.impl.IfStatementInstruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class IfStatementResult extends ResultType {

	private final InstructionDefinitions defs;
	private final ArrayList<IfStatementInstruction.IfStatement> andInstructions;
	private final ArrayList<ResultType> resultTypes;

	public IfStatementResult(InstructionDefinitions defs, ArrayList<IfStatementInstruction.IfStatement> andInstructions, ArrayList<ResultType> resultTypes) {
		this.defs = defs;
		this.andInstructions = andInstructions;
		this.resultTypes = resultTypes;
	}

	public void print(Printer printer) {
		printer.print("if(");
		for(int i = 0; i < andInstructions.size(); i++) {
			IfStatementInstruction.IfStatement ifStatement = andInstructions.get(i);
			ifStatement.left().print(printer);
			printer.print(" "+getExpressionSymbol(defs)+" ");
			ifStatement.right().print(printer);
			if(i < andInstructions.size() - 1) {
				printer.print(" && ");
			}
		}
		//TODO - && || support
		printer.print(") {");
		printer.indent();
		printer.newLine();
		resultTypes.forEach(resultType -> {
			printer.printIndent();
			resultType.print(printer);
			printer.newLine();
		});
		printer.outdent();
		printer.printIndent();
		printer.print("}");
	}

	public String getExpressionSymbol(InstructionDefinitions defs) {
		return switch(defs) {
			case INT_LT, LONG_LT -> "<";
			case INT_LE, LONG_LE -> "<=";
			case INT_GE, LONG_GE -> ">=";
			case INT_GT, LONG_GT -> ">";
			case INT_EQ, LONG_EQ -> "==";
			case INT_NE, LONG_NE -> "!=";
			default -> throw new IllegalStateException("Unexpected value: " + defs);
		};
	}

}
