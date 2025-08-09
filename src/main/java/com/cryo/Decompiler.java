package com.cryo;

import com.cryo.cache.Cache;

import java.io.IOException;

public class Decompiler {

	public static void main(String[] args) throws IOException {
		Cache.init(Settings.PACKED_PATH);

		CS2Script script = new CS2Script(11);

		System.out.println(script.getArguments().size() + " arguments: "+
				script.getArguments().values().stream()
						.map(arg -> arg.name() + " (" + arg.type() + ")")
						.reduce((a, b) -> a + ", " + b).orElse("none")
		);

		System.out.println(script.getVariables().size() + " variables: "+
				script.getVariables().values().stream()
						.map(var -> var.name() + " (" + var.type() + ")")
						.reduce((a, b) -> a + ", " + b).orElse("none")
		);

		System.out.println(script.getInstructions().size() +" instructions: "+
				script.getInstructions().stream()
						.map(instruction -> instruction.getDefinitions().name() + " " + instruction.getValue())
						.reduce((a, b) -> a + ", " + b).orElse("none")
		);

		script.process();

		script.print();
		//script.getInstructions();

		//script.print();
	}
}
