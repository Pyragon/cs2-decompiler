package com.cryo.entities.resulttypes.impl;

import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class StoreVarResult extends ResultType {

	private final InstructionDefinitions defs;
	private final ResultType value;
	private final int id;

	public StoreVarResult(InstructionDefinitions defs, ResultType value, int id) {
		this.defs = defs;
		this.value = value;
		this.id = id;
	}

	@Override
	public void print(Printer printer) {
		String name = switch(defs) {
			case STORE_VARC -> "varclient";
			case STORE_VARC_STRING -> "varclientstring";
			case STORE_VARPBIT -> "varplayerbit";
			case STORE_VARP -> "varplayer";
			default -> throw new IllegalStateException("Unexpected definition: " + defs);
		};
		name = name + "_"+id;
		printer.print(name + " = ");
		value.print(printer);
	}
}
