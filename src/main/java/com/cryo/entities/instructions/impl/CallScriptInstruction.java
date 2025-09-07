package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.db.ScriptDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.CallScriptResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;
import java.util.Collections;

public class CallScriptInstruction extends Instruction {

	public CallScriptInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		int scriptId = (int) value;
		ScriptDefinitions scriptDefinitions = ScriptDefinitions.getScript(scriptId);
		if(scriptDefinitions == null) {
			throw new IllegalStateException("No script definitions found for script id: " + scriptId);
		}
		ArrayList<ResultType> arguments = new ArrayList<>();
		for(Type argumentType : scriptDefinitions.getArgTypes())
			arguments.add(script.getStack(argumentType).pop());
		Collections.reverse(arguments);
		CallScriptResult result = new CallScriptResult(scriptId, arguments);
		if(scriptDefinitions.getReturnType() == null || scriptDefinitions.getReturnType()[0] == Type.VOID) {
			if(results == null)
				results = script.getResults();
			results.add(result);
		} else if(scriptDefinitions.getReturnType().length > 1)
			throw new IllegalStateException("Multiple return types not supported yet on script: "+script.getId()+" for call cs2: "+value);
		else
			script.getStack(scriptDefinitions.getReturnType()[0]).push(result);
	}
}
