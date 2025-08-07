package com.cryo.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Printer {

	private final String path;
	private int indents;

	private BufferedWriter writer;

	public Printer(String path) {
		this.path = path;
		init();
	}

	public Printer(int scriptId) {
		this.path = "./data/decompiled/" + scriptId + ".ts";
		init();
	}

	public void indent() {
		indents++;
	}

	public void outdent() {
		indents--;
		if(indents < 0) indents = 0;
	}

	public void newLine() {
		try {
			writer.newLine();
		} catch (IOException e) {
			Logger.err(this.getClass(), "Failed to write new line to printer for path: " + this.path);
			Logger.err(this.getClass(), e.getMessage() + "\n" + e.getMessage());
		}
	}

	public void print(String data) {
		try {
			writer.write("\t".repeat(indents));
			writer.write(data);
		} catch (IOException e) {
			Logger.err(this.getClass(), "Failed to write to printer for path: " + this.path + ", data: " + data);
			Logger.err(this.getClass(), e.getMessage() + "\n" + e.getMessage());
		}
	}

	public void save() {
		try {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
			Logger.err(this.getClass(), "Failed to save printer for path: " + this.path);
			Logger.err(this.getClass(), e.getMessage() + "\n" + e.getMessage());
		}
	}

	private void init() {
		try {
			writer = new BufferedWriter(new FileWriter(this.path, false));
		} catch(IOException e) {
			throw new RuntimeException("Failed to initialize printer for path: " + this.path, e);
		}
	}
}
