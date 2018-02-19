package com.whelch.ledcontroller.model;

public enum StateType {
	strip((byte)0),
	rainbow((byte)1),
	breathing((byte)2),
	pingPong((byte)3);
	
	public final byte value;
	
	StateType(byte val) {
		value = val;
	}
}
