package com.cryo.entities;

public enum Type {
	INT, STRING, LONG;

	public static Type fromString(String type) {
		return switch (type.toLowerCase()) {
			case "ic", "i" -> INT;
			case "s" -> STRING;
			case "l" -> LONG;
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		};
	}
}
