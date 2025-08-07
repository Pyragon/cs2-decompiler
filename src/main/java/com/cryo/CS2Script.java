package com.cryo;

import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.entities.*;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.io.InputStream;
import com.cryo.utils.Logger;
import com.cryo.utils.Printer;

import java.util.*;

public class CS2Script {

	private final int id;
	private Type returnType;
	private HashMap<Integer, Variable> variables;
	private HashMap<Integer, Variable> arguments;
	private HashMap<Integer, ArrayList<SwitchCase>> switches;
	private Stack<Instruction> instructions;

	public CS2Script(int id) {
		this.id = id;
		init();
	}

	public void init() {
		byte[] data = Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).getFile(id, 0);
		if(data == null || data.length == 0) {
			Logger.err(this.getClass(), "Invalid data for script id: " + id);
			return;
		}
		InputStream stream = new InputStream(data);
		decode(stream);
	}

	public void decode(InputStream stream) {

		stream.setOffset(stream.getLength() - 2);

		int switchBlockSize = stream.readUnsignedShort();
		int instructionLength = stream.getLength() - 2 - switchBlockSize - 16;

		stream.setOffset(instructionLength);

		stream.readInt(); //codeSize

		int intVarsCount = stream.readUnsignedShort();
		int stringVarsCount = stream.readUnsignedShort();
		int longVarsCount = stream.readUnsignedShort();

		int intArgsCount = stream.readUnsignedShort();
		int stringArgsCount = stream.readUnsignedShort();
		int longArgsCount = stream.readUnsignedShort();

		variables = new HashMap<>();
		arguments = new HashMap<>();

		int argIndex = 0;
		int variableIndex = 0;
		int[] typeIndexes = {0, 0, 0}; // int, string, long indexes
		Type[] types = {Type.INT, Type.STRING, Type.LONG};
		int[] argCounts = {intArgsCount, stringArgsCount, longArgsCount};
		int[] localCounts = {intVarsCount, stringVarsCount, longVarsCount};
		String[] prefixes = {"i", "s", "l"};

		// Add all arguments first
		for (int t = 0; t < 3; t++) {
			for (int i = 0; i < argCounts[t]; i++) {
				String name = "arg" + argIndex;
				this.arguments.put(argIndex, new Variable(argIndex, types[t], name, true));
				this.variables.put(variableIndex++, new Variable(typeIndexes[t]++, types[t], name, true));
				argIndex++;
			}
		}

		// Add local variables
		for (int t = 0; t < 3; t++) {
			for (int i = 0; i < localCounts[t] - argCounts[t]; i++) {
				String name = prefixes[t] + "Var" + i;
				this.variables.put(variableIndex++, new Variable(typeIndexes[t]++, types[t], name, false));
			}
		}

		int switchCount = stream.readUnsignedByte();

		if(switchCount > 0) {
			switches = new HashMap<>();
			for(int i = 0; i < switchCount; i++) {
				switches.put(i, new ArrayList<>());
				int size = stream.readUnsignedShort();
				while(size-- > 0) {
					switches.get(i).add(new SwitchCase(stream.readInt(), stream.readInt()));
				}
			}
		}

		stream.setOffset(0);

		String name = stream.readNullString();

		instructions = new Stack<>();

		while(stream.getOffset() < instructionLength) {
			int opcode = stream.readUnsignedShort();
			if(opcode < 0) {
				Logger.err(this.getClass(), "Invalid opcode: " + opcode + " in script id: " + id);
				return;
			}
			Object value;
			InstructionDefinitions defs = InstructionDefinitions.getByOpcode(opcode);
			if(defs == null) {
				Logger.err(this.getClass(), "Unknown opcode: " + opcode + " in script id: " + id);
				return;
			}
			switch(defs) {
				case PUSH_STRING -> value = stream.readString();
				case PUSH_LONG -> value = stream.readLong();
				default -> value = defs.hasExtra() ? stream.readInt() : stream.readUnsignedByte();
			}
			Instruction instruction = new Instruction(defs, value);
			instructions.push(instruction);
		}
	}

	public void print() {
		Printer printer = new Printer(1);
		printer.print("function script_"+id+"(");
		for (int i = 0; i < arguments.size(); i++) {
			Variable arg = arguments.get(i);
			if (i > 0) {
				printer.print(", ");
			}
			printer.print(arg.name() + ": " + arg.type().name().toLowerCase());
		}
		printer.print("): ");
		printer.print(returnType != null ? returnType.name().toLowerCase() : "void {");
		printer.indent();
		printer.newLine();

		printer.newLine();
		printer.outdent();
		printer.print("}");
		printer.save();
	}

	public record Variable(int index, Type type, String name, boolean isArgument) {}

	public int getId() {
		return id;
	}

	public HashMap<Integer, Variable> getVariables() {
		return variables;
	}

	public HashMap<Integer, Variable> getArguments() {
		return arguments;
	}
}
