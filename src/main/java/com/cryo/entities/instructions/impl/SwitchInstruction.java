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
			int size = (int) instruction.getValue();
			if(i != cases.size() - 1)
				size = cases.get(i + 1).getAddress() - switchCase.getAddress() - 1;
			int address = iterator.getIndex() + size;
			ArrayList<ResultType> scope = new ArrayList<>();
			while(address > iterator.getIndex()) {
				instruction = iterator.next();
				if(instruction.getDefinitions() == InstructionDefinitions.GOTO) {
					if(i != cases.size() - 1) {
						throw new RuntimeException("Unexpected GOTO in switch case at address: " + (iterator.getIndex() - 1) + " before end of case at address: " + (cases.get(i + 1).getAddress()));
					}
					ArrayList<ResultType> defaultScope = new ArrayList<>();
					int gotoSize = (int) instruction.getValue();
					int gotoAddress = iterator.getIndex() + gotoSize;
					while(gotoAddress > iterator.getIndex()) {
						instruction = iterator.next();
						instruction.process(iterator, defaultScope);
					}
					defaultCase = new CaseResult(null, defaultScope, true);
					break;
				}
				instruction.process(iterator, scope);
			}
			scopes.add(new CaseResult(switchCases, scope));
		}
//		if((instruction = iterator.peek()).getDefinitions() == InstructionDefinitions.GOTO) {
//			int size = (int) instruction.getValue();
//			int address = iterator.getIndex() + size;
//			ArrayList<ResultType> scope = new ArrayList<>();
//			iterator.next();
//			while(address > iterator.getIndex()) {
//				instruction = iterator.next();
//				instruction.process(iterator, scope);
//			}
//			defaultCase = new CaseResult(null, scope, true);
//		}
		if(results == null) results = script.getResults();
		results.add(new SwitchResult(switchValue, scopes, defaultCase));
	}
}
