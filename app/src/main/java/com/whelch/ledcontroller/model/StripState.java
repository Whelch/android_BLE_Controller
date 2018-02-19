package com.whelch.ledcontroller.model;

public class StripState {
	public boolean leftPost;
	public boolean leftRail;
	public boolean rightPost;
	public boolean rightRail;
	
	public StripState() {}
	
	public StripState(boolean leftPost, boolean leftRail, boolean rightPost, boolean rightRail) {
		this.leftPost = leftPost;
		this.leftRail = leftRail;
		this.rightPost = rightPost;
		this.rightRail = rightRail;
	}
}
