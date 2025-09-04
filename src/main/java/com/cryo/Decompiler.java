package com.cryo;

import com.cryo.cache.Cache;
import com.cryo.db.ScriptDefinitions;
import com.cryo.utils.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Decompiler {

	public static void main(String[] args) throws IOException {
		Cache.init(Settings.PACKED_PATH);

		ScriptDefinitions.loadDefinitions();

		CS2Script script = new CS2Script(2091);

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

		//print out script.getSwitches with addresses
		System.out.println(script.getSwitches().size() + " switches: " +
				script.getSwitches().entrySet().stream()
						.map(entry -> "Switch ID " + entry.getKey() + " with cases: " +
								entry.getValue().stream()
										.map(switchCase -> "case " + switchCase.getCaseNum() + " at address " + switchCase.getAddress())
										.reduce((a, b) -> a + ", " + b).orElse("none"))
						.reduce((a, b) -> a + "; " + b).orElse("none")
		);

		script.process();

		script.getResults().forEach(r -> System.out.println(r.getClass().getSimpleName()));

		script.print();
		//script.getInstructions();

		//script.print();
	}
}
