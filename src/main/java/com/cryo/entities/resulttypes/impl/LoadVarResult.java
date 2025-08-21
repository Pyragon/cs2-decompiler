package com.cryo.entities.resulttypes.impl;

import com.cryo.db.InstructionDefinitions;
import com.cryo.entities.resulttypes.ResultType;
import com.cryo.utils.Printer;

public class LoadVarResult extends ResultType {

	private final InstructionDefinitions defs;
	private final int id;

	public LoadVarResult(InstructionDefinitions defs, int id) {
		this.defs = defs;
		this.id = id;
	}

	@Override
	public void print(Printer printer) {
		String name = switch(defs) {
			case LOAD_VARP -> "varplayer";
			case LOAD_VARPBIT -> "varplayerbit";
			case LOAD_VARC -> "varclient";
			case LOAD_VARC_STRING -> "varclientstring";
			case LOAD_CLAN_SETTING_VARBIT -> "clansettingvarbit";
			case LOAD_CLAN_SETTING_VAR -> "clansettingvar";
			case LOAD_CLAN_SETTING_VAR_LONG -> "clansettingvarlong";
			case LOAD_CLAN_SETTING_VAR_STRING -> "clansettingvarstring";
			case LOAD_CLAN_VAR -> "clanvar";
			case LOAD_CLAN_VARBIT -> "clanvarbit";
			default -> throw new IllegalStateException("Unexpected definition: " + defs);
		};
		name = name + "_" + id;
		printer.print(name);
	}
}
