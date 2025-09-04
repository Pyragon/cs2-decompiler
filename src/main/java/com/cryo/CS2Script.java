package com.cryo;

import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.db.ScriptDefinitions;
import com.cryo.entities.*;
import com.cryo.entities.instructions.Instruction;
import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.io.InputStream;
import com.cryo.utils.Logger;
import com.cryo.utils.PeekableIterator;
import com.cryo.utils.Printer;

import java.util.*;

public class CS2Script {

	private final int id;
	private HashMap<Integer, Variable> variables;
	private HashMap<Integer, Variable> arguments;

	// New data structures for type-specific access
	private HashMap<Type, HashMap<Integer, Variable>> variablesByType;
	private HashMap<Type, HashMap<Integer, Variable>> argumentsByType;

	private HashMap<Integer, ArrayList<SwitchCase>> switches;
	private LinkedList<Instruction> instructions;

	private final Stack<ResultType> intStack;
	private final Stack<ResultType> stringStack;
	private final Stack<ResultType> longStack;

	private final ArrayList<ResultType> results;

	private ScriptDefinitions defs;

	public CS2Script(int id) {
		this.id = id;
		this.intStack = new Stack<>();
		this.stringStack = new Stack<>();
		this.longStack = new Stack<>();
		this.results = new ArrayList<>();
		init();
	}

	public void init() {
		defs = ScriptDefinitions.getScript(id);
		if(defs == null) {
			Logger.err(this.getClass(), "No script definitions found for id: " + id);
			return;
		}
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

		// Initialize type-specific maps
		variablesByType = new HashMap<>();
		argumentsByType = new HashMap<>();
		Type[] types = {Type.INT, Type.STRING, Type.LONG};
		for (Type type : types) {
			variablesByType.put(type, new HashMap<>());
			argumentsByType.put(type, new HashMap<>());
		}

		int argIndex = 0;
		int variableIndex = 0;
		int[] typeCounters = {0, 0, 0}; // int, string, long type-specific counters
		int[] argCounts = {intArgsCount, stringArgsCount, longArgsCount};
		int[] localCounts = {intVarsCount, stringVarsCount, longVarsCount};
		String[] prefixes = {"int", "string", "long"};

		// Add all arguments first
		for (int t = 0; t < types.length; t++) {
			Type currentType = types[t];
			for (int i = 0; i < argCounts[t]; i++) {
				String name = prefixes[t] + typeCounters[t];
				Variable variable = new Variable(variableIndex, currentType, name, true);

				// Store in existing maps
				this.arguments.put(argIndex++, variable);
				this.variables.put(variableIndex++, variable);

				// Store in type-specific maps
				argumentsByType.get(currentType).put(typeCounters[t], variable);
				variablesByType.get(currentType).put(typeCounters[t], variable);

				typeCounters[t]++;
			}
		}

		// Add local variables
		for (int t = 0; t < types.length; t++) {
			Type currentType = types[t];
			for (int i = 0; i < localCounts[t] - argCounts[t]; i++) {
				String name = prefixes[t] + typeCounters[t];
				Variable variable = new Variable(variableIndex, currentType, name, false);

				// Store in existing maps
				this.variables.put(variableIndex++, variable);

				// Store in type-specific map
				variablesByType.get(currentType).put(typeCounters[t], variable);

				typeCounters[t]++;
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

		//TODO - Should probably actually find out why this happens and figure out a better way to do this.
		if(defs.getReturnType() != Type.VOID) {
			Logger.log(this.getClass(), "Script " + id + " has a return type: " + defs.getReturnType().name() + ".");
			Logger.log(this.getClass(), "Removing the last two instructions");
			Logger.log(this.getClass(), "Old: "+instructionLength+". New: "+ (instructionLength - 2) + ".");
			if(instructions.size() < 2) {
				Logger.err(this.getClass(), "Not enough instructions to remove the last two for script id: " + id);
				return;
			}
			instructions.removeLast(); // Remove the last instruction (usually a return)
			instructions.removeLast(); // Remove the second last instruction (usually a return value push)
		}
	}

	public void process() {
		PeekableIterator<Instruction> it = new PeekableIterator<>(instructions);
		while(it.hasNext()) {
			Instruction instruction = it.next();
			try {
				instruction.process(it, null);
				Logger.log(this.getClass(), "Processed instruction: " + instruction.getDefinitions().name());Logger.log(this.getClass(), "Current stack: "+
						"intStack=" + getStack(Type.INT).size() +
						", stringStack=" + getStack(Type.STRING).size() +
						", longStack=" + getStack(Type.LONG).size()
				);
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
		printer.print(defs.getReturnType() != null ? defs.getReturnType().name().toLowerCase() : "void");
		printer.print(" {");
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
			default -> throw new IllegalArgumentException("Invalid stack type: " + type);
		};
	}

	// New methods for type-specific variable access

	/**
	 * Get a variable by its type and type-specific index
	 * @param type The variable type (INT, STRING, LONG)
	 * @param index The type-specific index (e.g., 0 for first string variable)
	 * @return The variable, or null if not found
	 */
	public Variable getVariableByType(Type type, int index) {
		return variablesByType.get(type).get(index);
	}

	/**
	 * Get an argument by its type and type-specific index
	 * @param type The argument type (INT, STRING, LONG)
	 * @param index The type-specific index (e.g., 0 for first string argument)
	 * @return The argument variable, or null if not found
	 */
	public Variable getArgumentByType(Type type, int index) {
		return argumentsByType.get(type).get(index);
	}

	/**
	 * Get all variables of a specific type
	 * @param type The variable type
	 * @return Map of type-specific index to Variable
	 */
	public Map<Integer, Variable> getVariablesOfType(Type type) {
		return variablesByType.get(type);
	}

	/**
	 * Get all arguments of a specific type
	 * @param type The argument type
	 * @return Map of type-specific index to Variable
	 */
	public Map<Integer, Variable> getArgumentsOfType(Type type) {
		return argumentsByType.get(type);
	}

	// Existing getters
	public ScriptDefinitions getDefs() {
		return defs;
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

	public HashMap<Integer, ArrayList<SwitchCase>> getSwitches() {
		return switches;
	}
}