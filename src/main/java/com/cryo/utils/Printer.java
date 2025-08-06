package com.cryo.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Printer {

	private final String path;

	private BufferedWriter writer;

	public Printer(String path) {
		this.path = path;
		init();
	}

	public Printer(int scriptId) {
		this.path = "scripts/" + scriptId + ".js";
		init();
	}

	private void init() {
		try {
			writer = new BufferedWriter(new FileWriter(this.path, false));
		} catch(IOException e) {
			throw new RuntimeException("Failed to initialize printer for path: " + this.path, e);
		}
	}
}
