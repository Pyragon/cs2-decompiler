package com.cryo.entities.instructions.impl;

import com.cryo.CS2Script;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.SwitchCase;
import com.cryo.entities.Type;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.entities.resulttypes.impl.CaseResult;
import com.cryo.entities.resulttypes.impl.SwitchResult;
import com.cryo.utils.Logger;
import com.cryo.utils.PeekableIterator;

import java.util.ArrayList;
import java.util.HashMap;

public class SwitchInstruction extends Instruction {

	public SwitchInstruction(InstructionDefinitions defs, CS2Script script, Object value) {
		super(defs, script, value);
	}

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		ResultType switchValue = script.getStack(Type.INT).pop();
		if(switchValue == null) {
			throw new IllegalStateException("Switch value is null for instruction: " + defs.name());
		}
		ArrayList<SwitchCase> cases = script.getSwitches().get((int) value);
		if(cases == null) {
			throw new IllegalStateException("No cases found for switch id: " + value);
		}
		ArrayList<CaseResult> scopes = new ArrayList<>();
		CaseResult defaultCase = null;
		Instruction instruction = iterator.peek();
		if(instruction.getDefinitions() != InstructionDefinitions.GOTO) {
			ArrayList<ResultType> scope = new ArrayList<>();
			while((instruction = iterator.peek()).getDefinitions() != InstructionDefinitions.GOTO) {
				iterator.next();
				instruction.process(iterator, scope);
			}
			defaultCase = new CaseResult(null, scope, true);

		}
		for(int i = 0; i < cases.size(); i++) {
			ArrayList<SwitchCase> switchCases = new ArrayList<>();
			SwitchCase switchCase = cases.get(i);
			switchCases.add(switchCase);
			while(true) {
				if(i == cases.size() - 1) break;
				SwitchCase nextCase = cases.get(i + 1);
				if(switchCase.getAddress() == nextCase.getAddress()) {
					switchCases.add(nextCase);
					i++;
				} else
					break;
			}
			instruction = iterator.next();
			if(instruction.getDefinitions() != InstructionDefinitions.GOTO) {
				throw new IllegalStateException("Expected GOTO instruction at address: " + switchCase.getAddress() + " but found: " + instruction.getDefinitions().name());
			}
			ArrayList<ResultType> scope = new ArrayList<>();
			while((instruction = iterator.peek()).getDefinitions() != InstructionDefinitions.GOTO) {
				iterator.next();
				instruction.process(iterator, scope);
			}
			scopes.add(new CaseResult(switchCases, scope));
		}
		if((instruction = iterator.peek()).getDefinitions() == InstructionDefinitions.GOTO) {
			int size = (int) instruction.getValue();
			ArrayList<ResultType> scope = new ArrayList<>();
			iterator.next();
			for(int i = 0; i < size; i++) {
				instruction = iterator.next();
				instruction.process(iterator, scope);
			}
			defaultCase = new CaseResult(null, scope, true);
		}
		script.getResults().add(new SwitchResult(switchValue, scopes, defaultCase));
	}
}
