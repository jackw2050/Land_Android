package com.zlscorp.ultragrav.type;

public enum ObservationType {

	SINGLE(1), CONTINUOUS(2), READ_METER(3);

	private int id;

	private ObservationType(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	public int getId() {
		return id;
	}
}
