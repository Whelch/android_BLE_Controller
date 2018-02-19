package com.whelch.ledcontroller.model;

public class RainbowState {
	public boolean active;
	public byte repeat;
	public boolean flow;
	public byte duration;
	
	public RainbowState() {}
	
	public RainbowState(boolean active, byte repeat, boolean flow, byte duration) {
		this.active = active;
		this.repeat = repeat;
		this.flow = flow;
		this.duration = duration;
	}
}
