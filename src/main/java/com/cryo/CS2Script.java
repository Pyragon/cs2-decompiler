package com.cryo;

import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.entities.*;
import com.cryo.entities.instructions.Instruction;
import com.cryo.entities.instructions.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
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
	private LinkedList<Instruction> instructions;

	private final Stack<ResultType> intStack;
	private final Stack<ResultType> stringStack;
	private final Stack<ResultType> longStack;

	private final ArrayList<ResultType> results;

	public CS2Script(int id) {
		this.id = id;
		this.intStack = new Stack<>();
		this.stringStack = new Stack<>();
		this.longStack = new Stack<>();
		this.results = new ArrayList<>();
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
		String[] prefixes = {"int", "string", "long"};

		// Add all arguments first
		for (int t = 0; t < 3; t++) {
			for (int i = 0; i < argCounts[t]; i++) {
				String name = "arg" + argIndex;
				this.arguments.put(argIndex, new Variable(argIndex, types[t], name, true));
				this.variables.put(variableIndex++, new Variable(typeIndexes[t]++, types[t], name, true)); //typeIndexes should be variableIndex
				argIndex++;
			}
		}

		// Add local variables
		for (int t = 0; t < 3; t++) {
			for (int i = 0; i < localCounts[t] - argCounts[t]; i++) {
				String name = prefixes[t] + i;
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

		instructions = new LinkedList<>();

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
			Instruction instruction = getInstructionClassFromDefs(defs, value);
			if(instruction == null) {
				Logger.err(this.getClass(), "No class added yet for instruction: "+defs.name());
				continue;
			}
			instructions.add(instruction);
		}
	}

	public void process() {
		for(Iterator<Instruction> it = instructions.iterator(); it.hasNext();) {
			Instruction instruction = it.next();
			try {
				Logger.log(this.getClass(), "Processing instruction: "+instruction.getDefinitions().name());
				instruction.process(it, null);
			} catch (Exception e) {
				Logger.err(this.getClass(), "Error processing instruction: " + instruction.getDefinitions().name() + " in script id: " + id);
				e.printStackTrace();
				return;
			}
		}
	}

	public void print() {
		Printer printer = new Printer(id);
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

		results.forEach(result -> result.print(printer, true));

		printer.outdent();
		printer.print("}");
		printer.save();
	}

	public Instruction getInstructionClassFromDefs(InstructionDefinitions defs, Object value) {
		if(defs.getClazz() == null) return null;
		try {
			Instruction instruction = defs.getClazz().getConstructor(InstructionDefinitions.class, CS2Script.class, Object.class)
					.newInstance(defs, this, value);
			return instruction;
		} catch (ReflectiveOperationException e) {
			Logger.err(this.getClass(), "Failed to create instruction for: " + defs.name() + " in script id: " + id);
		}
		return null;
	}

	public Stack<ResultType> getStack(Type type) {
		return switch(type) {
			case INT -> intStack;
			case STRING -> stringStack;
			case LONG -> longStack;
		};
	}

	public ArrayList<ResultType> getResults() {
		return results;
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

	public LinkedList<Instruction> getInstructions() {
		return instructions;
	}
}
