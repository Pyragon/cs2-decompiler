package com.cryo.db;

import com.cryo.entities.Type;
import com.cryo.utils.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

public class ScriptDefinitions {

	private final int id;
	private final String name;
	private final Type[] argTypes;
	private final Type[] returnType;

	private static HashMap<Integer, ScriptDefinitions> definitions;

	public ScriptDefinitions(int id, String name, Type[] argTypes, Type[] returnType) {
		this.id = id;
		this.name = name;
		this.argTypes = argTypes;
		this.returnType = returnType;
	}

	public static void loadDefinitions() {
		definitions = new HashMap<>();

		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new FileReader("./data/scripts.json"));

			JSONArray array = (JSONArray) obj;
			for(Object o : array) {

				JSONObject item = (JSONObject) o;

				int id = (int) ((long) item.get("id"));
				String name = (String) item.get("name");
				String[] argTypesString = ((String) item.get("argTypes")).trim().split(",");
				if(argTypesString.length == 1 && argTypesString[0].isEmpty())
					argTypesString = new String[0];
				Type[] argTypes = new Type[argTypesString.length];
				for(int i = 0; i < argTypesString.length; i++) {
					try {
						Type type = Type.fromString(argTypesString[i].trim());
						if(type == null) continue;
						argTypes[i] = type;
					} catch(Exception e) {
						Logger.err(ScriptDefinitions.class, "Invalid type for script id: " + id + ", type: " + argTypesString[i] + ", name: " + name);
					}
				}
				Type[] returnType;
				if(!item.containsKey("returnType"))
					returnType = new Type[] { Type.VOID };
				else {
					String returnTypeString = (String) item.get("returnType");
					if(!returnTypeString.contains(","))
						returnType = new Type[] { Type.fromString(returnTypeString) };
					else
						returnType = Stream.of(returnTypeString.split(",")).map(Type::fromString).toArray(Type[]::new);
				}
				ScriptDefinitions definition = new ScriptDefinitions(id, name, argTypes, returnType);
				definitions.put(id, definition);
			}
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static ScriptDefinitions getScript(int id) {
		return definitions.get(id);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Type[] getArgTypes() {
		return argTypes;
	}

	public Type[] getReturnType() {
		return returnType;
	}



}
