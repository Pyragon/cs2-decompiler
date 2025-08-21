package com.cryo.entities.resulttypes.impl;

import com.cryo.entities.Type;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class LiteralResult extends ResultType {

	private final Object value;
	private final Type type;

	public LiteralResult(Object value, Type type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public void print(Printer printer) {
		switch(type) {
			case INT -> printer.print(Integer.toString((int) value));
			case LONG -> printer.print(Long.toString((long) value));
			case STRING -> printer.print((String) value);
		}
	}

	public Type getType() {
		return type;
	}
}
