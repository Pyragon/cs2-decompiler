package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class EnumResult extends ResultType {

	private final LiteralResult keyType;
	private final LiteralResult valueType;
	private final ResultType enumId;
	private final ResultType valueId;

	public EnumResult(LiteralResult keyType, LiteralResult valueType, ResultType enumId, ResultType valueId) {
		this.keyType = keyType;
		this.valueType = valueType;
		this.enumId = enumId;
		this.valueId = valueId;
	}

	@Override
	public void print(Printer printer) {
		printer.print("enum(");
		printer.print("\""+((char) ((int) keyType.getValue()))+"\"");
		printer.print(", ");
		printer.print("\""+((char) ((int) valueType.getValue()))+"\"");
		printer.print(", ");
		enumId.print(printer);
		printer.print(", ");
		valueId.print(printer);
		printer.print(")");
	}
}
