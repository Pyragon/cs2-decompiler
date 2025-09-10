package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.db.ScriptDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.CallScriptResult;
import com.cryo.entities.resulttypes.impl.MultiStoreVariableResult;
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
		if(results == null)
			results = script.getResults();
		if(scriptDefinitions.getReturnType().length == 1) {
			if(scriptDefinitions.getReturnType()[0] == Type.VOID)
				results.add(new CallScriptResult(scriptId, arguments));
			else
				script.getStack(scriptDefinitions.getReturnType()[0]).push(new CallScriptResult(scriptId, arguments));
		} else {
			ArrayList<CS2Script.Variable> variables = new ArrayList<>();
			for(int i = 0; i < scriptDefinitions.getReturnType().length; i++) {
				Instruction nextInstruction = iterator.next();
				if(!(nextInstruction instanceof StoreVariableInstruction)) {
					throw new IllegalStateException("Expected StoreVariableInstruction after multi-return script, but got: " + nextInstruction.getDefinitions().name());
				}
				int index = (int) nextInstruction.getValue();
				Type type;
				switch(nextInstruction.getDefinitions()) {
					case STORE_INT -> type = Type.INT;
					case STORE_LONG -> type = Type.LONG;
					case STORE_STRING -> type = Type.STRING;
					default -> throw new IllegalArgumentException("Invalid instruction definition for StoreVariableInstruction: " + defs);
				}
				if(!script.getVariablesOfType(type).containsKey(index)) {
					throw new IllegalArgumentException("Variable with index " + index + " and type " + type + " does not exist in the script variables.");
				}
				CS2Script.Variable variable = script.getVariableByType(type, index);
				if(variable.type() != type) {
					throw new IllegalArgumentException("Variable type mismatch: expected " + type + " but found " + variable.type() + " for variable index " + index);
				}
				variables.add(variable);
			}
			Collections.reverse(variables);
			results.add(new MultiStoreVariableResult(variables.toArray(CS2Script.Variable[]::new), "script_"+scriptId, arguments));
		}
	}
}
