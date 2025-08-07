package com.cryo;

import com.cryo.cache.Cache;

import java.io.IOException;

public class Decompiler {

	public static void main(String[] args) throws IOException {
		Cache.init(Settings.PACKED_PATH);

		CS2Script script = new CS2Script(1);

		//print out script arguments
		System.out.println(script.getArguments().size() + " arguments: "+
				script.getArguments().values().stream()
						.map(arg -> arg.name() + " (" + arg.type() + ")")
						.reduce((a, b) -> a + ", " + b).orElse("none")
		);

		script.print();
		//script.getInstructions();

		//script.print();
	}
}
