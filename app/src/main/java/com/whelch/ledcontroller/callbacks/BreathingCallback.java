package com.whelch.ledcontroller.callbacks;

import com.whelch.ledcontroller.model.BreathingState;

public interface BreathingCallback {
	void onChange(BreathingState state);
}
