package com.cryo.entities.resulttypes.impl;

import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class ParameterResult extends ResultType {

	private final InstructionDefinitions defs;
	private final int paramId;
	private final ResultType struct;

	public ParameterResult(InstructionDefinitions defs, int paramId, ResultType struct) {
		this.defs = defs;
		this.paramId = paramId;
		this.struct = struct;
	}

	@Override
	public void print(Printer printer) {
		printer.print(defs.name()+"("+paramId);
		if(struct != null) {
			printer.print(", ");
			struct.print(printer);
		}
		printer.print(")");
	}
}
