package com.cryo.entities;

public class SwitchCase {

	private final int caseNum;
	private final int address;

	public SwitchCase(int caseNum, int address) {
		this.caseNum = caseNum;
		this.address = address;
	}

	public int getCaseNum() {
		return caseNum;
	}

	public int getAddress() {
		return address;
	}
}
