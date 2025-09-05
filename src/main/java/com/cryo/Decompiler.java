package com.cryo;

import com.cryo.cache.Cache;
import com.cryo.db.ScriptDefinitions;
import com.cryo.utils.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Decompiler {

	//TODO: Overall Tweaks:
	//TODO: Figure out there ++i, why are they doing this? How did they know?
	//TODO: Figure out how/when they are using template literals
	//TODO: Change 0/1 to false/true where applicable
	//TODO: Change component hashes to comp(i, c) where applicable
	//TODO: Figure out how they know what types to cast to

	public static void main(String[] args) throws IOException {
		Cache.init(Settings.PACKED_PATH);

		ScriptDefinitions.loadDefinitions();

		CS2Script script = new CS2Script(111);

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
		if(script.getSwitches() != null) {
			System.out.println(script.getSwitches().size() + " switches: " + script.getSwitches().entrySet().stream().map(entry -> "Switch ID " + entry.getKey() + " with cases: " + entry.getValue().stream().map(switchCase -> "case " + switchCase.getCaseNum() + " at address " + switchCase.getAddress()).reduce((a, b) -> a + ", " + b).orElse("none")).reduce((a, b) -> a + "; " + b).orElse("none"));
		}

		script.process();

		script.getResults().forEach(r -> System.out.println(r.getClass().getSimpleName()));

		script.print();
		//script.getInstructions();

		//script.print();
	}
}
