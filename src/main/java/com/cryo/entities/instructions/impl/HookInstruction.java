package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.HookResult;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.LiteralResult;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;
import java.util.Collections;

public class HookInstruction extends Instruction {

	public HookInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		ArrayList<ResultType> params = new ArrayList<>();
		if(defs.hasComponent()) {
			ResultType component = script.getStack(Type.INT).pop();
			if(!(component instanceof LiteralResult hashResult)) {
				throw new RuntimeException("Hook "+defs.name()+" component is not a literal!");
			}
			params.add(new LiteralResult(hashResult.getValue(), Type.INT));
		}
		ResultType hookParams = script.getStack(Type.STRING).pop();
		if(!(hookParams instanceof LiteralResult paramsResult)) {
			throw new RuntimeException("Hook "+defs.name()+" params is not a literal!");
		}
		String callbackParamsString = (String) paramsResult.getValue();
		ArrayList<ResultType> callbackParams = new ArrayList<>();
		if(!callbackParamsString.isEmpty()) {
			String[] paramTypes = callbackParamsString.split("");
			for(int i = paramTypes.length - 1; i >= 0; i--) {
				Type type = Type.fromString(paramTypes[i]);
				ResultType param = script.getStack(type).pop();
				callbackParams.add(param);
			}
		}
		Collections.reverse(callbackParams);
		ResultType scriptResult = script.getStack(Type.INT).pop();
		if(!(scriptResult instanceof LiteralResult scriptIdResult)) {
			throw new RuntimeException("Hook "+defs.name()+" script id is not a literal!");
		}
		int scriptId = (int) scriptIdResult.getValue();
		if(results == null)
			results = script.getResults();
		results.add(new HookResult(this, scriptId, callbackParams, params));
		//TODO - remove params from stack as well
	}
}
