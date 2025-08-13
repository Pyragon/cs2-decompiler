package com.cryo.entities;

public enum Type {
	INT, STRING, LONG;

	public static Type fromString(String type) {
		return switch (type.toLowerCase()) {
			case "ic", "i", "int" -> INT;
			case "s", "string" -> STRING;
			case "l", "long" -> LONG;
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		};
	}
}
