package com.whelch.ledcontroller.callbacks;

import com.whelch.ledcontroller.model.PingPongState;

public interface PingPongCallback {
	void onChange(PingPongState state);
}
