package com.cryo.entities;

public enum Type {
	INT, STRING, LONG, VOID;

	//TODO - ic, col
	public static Type fromString(String type) {
		return switch (type.toLowerCase()) {
			case "ic", "i", "int", "col", "bool", "z", "I" -> INT;
			case "s", "string" -> STRING;
			case "l", "long" -> LONG;
			case "void" -> VOID;
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		};
	}
}
