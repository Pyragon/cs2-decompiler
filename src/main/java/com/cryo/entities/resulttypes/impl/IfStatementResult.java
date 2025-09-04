package com.cryo.entities.resulttypes.impl;

import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.instructions.impl.IfStatementInstruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class IfStatementResult extends ResultType {

	private final InstructionDefinitions defs;
	private final ArrayList<IfStatementInstruction.IfStatement> ifStatements;
	private final ArrayList<ResultType> scope;
	private final ArrayList<ResultType> elseScope;

	public IfStatementResult(InstructionDefinitions defs, ArrayList<IfStatementInstruction.IfStatement> ifStatements, ArrayList<ResultType> scope, ArrayList<ResultType> elseScope) {
		this.defs = defs;
		this.ifStatements = ifStatements;
		this.scope = scope;
		this.elseScope = elseScope;
	}

	public void print(Printer printer) {
		printer.print("if(");
		for(int i = 0; i < ifStatements.size(); i++) {
			IfStatementInstruction.IfStatement ifStatement = ifStatements.get(i);
			if(ifStatement.statementType() != IfStatementInstruction.IfStatementType.DEFAULT) {
				if(ifStatement.statementType() == IfStatementInstruction.IfStatementType.AND) {
					printer.print(" && ");
				} else if(ifStatement.statementType() == IfStatementInstruction.IfStatementType.OR) {
					printer.print(" || ");
				} else {
					throw new IllegalStateException("Unknown IfStatementType: " + ifStatement.statementType());
				}
			}
			ifStatement.left().print(printer);
			printer.print(" "+getExpressionSymbol(defs)+" ");
			ifStatement.right().print(printer);
		}
		printer.print(") {");
		printer.indent();
		printer.newLine();
		scope.forEach(resultType -> resultType.print(printer, true));
		printer.outdent();
		printer.printIndent();
		if(elseScope.isEmpty())
			printer.print("}");
		else {
			printer.print("} else {");
			printer.indent();
			printer.newLine();
			elseScope.forEach(resultType -> resultType.print(printer, true));
			printer.outdent();
			printer.printIndent();
			printer.print("}");
		}
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
