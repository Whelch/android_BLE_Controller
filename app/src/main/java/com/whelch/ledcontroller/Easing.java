package com.whelch.ledcontroller;

public enum Easing {
	linear((byte)0),
	sinusoidal((byte)1),
	exponential((byte)2),
	quartic((byte)3);
	
	public final byte value;
	
	Easing(byte val) {
		value = val;
	}
	
	public static Easing fromByte(byte val) {
		switch(val) {
			case 0: return linear;
			case 1: return sinusoidal;
			case 2: return exponential;
			case 3: return quartic;
			default: return linear;
		}
	}
	
	@Override
	public String toString() {
		switch(value) {
			case 0: return "Linear";
			case 1: return "Sinusoidal";
			case 2: return "Exponential";
			case 3: return "Quartic";
			default: return "Unknown o _ O";
		}
	}
}
