package com.cryo.entities.resulttypes.impl;

import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.instructions.impl.StatementInstruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Logger;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class StatementResult extends ResultType {

	private final InstructionDefinitions defs;
	private final boolean isWhile;
	private final ArrayList<StatementInstruction.Statement> statements;
	private final ArrayList<ResultType> scope;
	private final ArrayList<ResultType> elseScope;

	public StatementResult(InstructionDefinitions defs, boolean isWhile, ArrayList<StatementInstruction.Statement> statements, ArrayList<ResultType> scope, ArrayList<ResultType> elseScope) {
		this.defs = defs;
		this.isWhile = isWhile;
		this.statements = statements;
		this.scope = scope;
		this.elseScope = elseScope;
	}

	public void print(Printer printer) {
		printer.print(isWhile ? "while(" : "if(");
		for(int i = 0; i < statements.size(); i++) {
			StatementInstruction.Statement statement = statements.get(i);
			if(statement.statementType() != StatementInstruction.StatementType.DEFAULT) {
				if(statement.statementType() == StatementInstruction.StatementType.AND) {
					printer.print(" && ");
				} else if(statement.statementType() == StatementInstruction.StatementType.OR) {
					printer.print(" || ");
				} else {
					throw new IllegalStateException("Unknown StatementType: " + statement.statementType());
				}
			}
			statement.left().print(printer);
			printer.print(" "+getExpressionSymbol(statement.defs())+" ");
			statement.right().print(printer);
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
