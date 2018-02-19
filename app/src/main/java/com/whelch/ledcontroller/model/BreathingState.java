package com.whelch.ledcontroller.model;

import com.whelch.ledcontroller.Easing;

public class BreathingState {
	public boolean active;
	public byte minIntensity;
	public byte maxIntensity;
	public byte duration;
	public Easing easing;
	
	public BreathingState() {}
	
	public BreathingState(boolean active, byte startIntensity, byte endIntensity, byte duration, Easing easing) {
		this.active = active;
		this.minIntensity = startIntensity;
		this.maxIntensity = endIntensity;
		this.duration = duration;
		this.easing = easing;
	}
}
