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

	//TODO - multipe cases with the same scope i.e
	/*
		switch(x) {
			case 1:
			case 2: {
				doSomething();
				break;
			}
		}
	 */
	//TODO - https://i.imgur.com/OTnovar.png
	//I think maybe the cases ending but still having a GOTO on there also means there's a default case?

	@Override
	public void process(PeekableIterator<Instruction> iterator, ArrayList<ResultType> results) {
		super.process(iterator, results);
		ResultType switchValue = script.getStack(Type.INT).pop();
		if(switchValue == null) {
			throw new IllegalStateException("Switch value is null for instruction: " + defs.name());
		}
		ArrayList<SwitchCase> cases = script.getSwitches().get((Integer) value);
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
			Logger.log(this.getClass(), "Processing switch case: "+switchCase.getCaseNum()+" "+switchCase.getAddress());
			instruction = iterator.next();
			int size;
			if(i == cases.size() - 1) {
				size = (int) instruction.getValue();
			} else {
				SwitchCase nextCase = cases.get(i + 1);
				size = nextCase.getAddress() - switchCase.getAddress() - 1;
			}
			if(instruction.getDefinitions() != InstructionDefinitions.GOTO) {
				throw new IllegalStateException("Expected GOTO instruction at address: " + switchCase.getAddress() + " but found: " + instruction.getDefinitions().name());
			}
			ArrayList<ResultType> scope = new ArrayList<>();
			for(int k = 0; k < size; k++) {
				Instruction scopedInstruction = iterator.next();
				scopedInstruction.process(iterator, scope);
			}
			scopes.add(new CaseResult(switchCases, scope));
		}
//		for(int i = 0; i < cases.size(); i++) {
//			SwitchCase switchCase = cases.get(i);
//			Logger.log(this.getClass(), "Processing switch case: "+switchCase.getCaseNum()+" "+switchCase.getAddress());
//			instruction = iterator.next();
//			int size;
//			if(i == cases.size() - 1) {
//				size = (int) instruction.getValue();
//			} else {
//				SwitchCase nextCase = cases.get(i + 1);
//				size = nextCase.getAddress() - switchCase.getAddress() - 1;
//			}
//			if(instruction.getDefinitions() != InstructionDefinitions.GOTO) {
//				throw new IllegalStateException("Expected GOTO instruction at address: " + switchCase.getAddress() + " but found: " + instruction.getDefinitions().name());
//			}
//			ArrayList<ResultType> scope = new ArrayList<>();
//			for(int k = 0; k < size; k++) {
//				Instruction scopedInstruction = iterator.next();
//				scopedInstruction.process(iterator, scope);
//			}
//			scopes.add(new CaseResult(switchCase, scope));
//		}
		script.getResults().add(new SwitchResult(switchValue, scopes, defaultCase));
	}
}
