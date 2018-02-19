package com.whelch.ledcontroller.callbacks;

import com.whelch.ledcontroller.model.StripState;

public interface StripCallback {
	void onChange(StripState state);
}
