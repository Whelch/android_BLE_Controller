package com.whelch.ledcontroller.model;

import com.whelch.ledcontroller.Easing;

public class PingPongState {
	public boolean active;
	public byte spread;
	public byte duration;
	public boolean dark;
	public Easing easing;
	
	public PingPongState() {}
	
	public PingPongState(boolean active, byte spread, byte duration, boolean dark, Easing easing) {
		this.active = active;
		this.spread = spread;
		this.duration = duration;
		this.dark = dark;
		this.easing = easing;
	}
}
