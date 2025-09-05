package com.cryo.entities.resulttypes;

import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.impl.HookInstruction;
import com.cryo.utils.Printer;

import java.util.ArrayList;

public class HookResult extends ResultType {

	private final HookInstruction instruction;
	private final int scriptId;
	private final ArrayList<ResultType> callbackParams;
	private final ArrayList<ResultType> params;

	public HookResult(HookInstruction instruction, int scriptId, ArrayList<ResultType> callbackParams, ArrayList<ResultType> params) {
		this.instruction = instruction;
		this.scriptId = scriptId;
		this.callbackParams = callbackParams;
		this.params = params;
	}

	@Override
	public void print(Printer printer) {
		printer.print(instruction.getDefinitions().name()+"(");
		printer.print("callback(");
		if(scriptId != -1)
			printer.print("script_"+scriptId);
		if(!callbackParams.isEmpty()) {
			printer.print(", ");
			for(int i = 0; i < callbackParams.size(); i++) {
				callbackParams.get(i).print(printer);
				if(i < callbackParams.size() - 1) {
					printer.print(", ");
				}
			}
		}
		printer.print(")");
		if(params != null && !params.isEmpty()) {
			printer.print(", ");
			for(int i = 0; i < params.size(); i++) {
				params.get(i).print(printer);
				if(i < params.size() - 1) {
					printer.print(", ");
				}
			}
		}
		printer.print(")");
	}
}
