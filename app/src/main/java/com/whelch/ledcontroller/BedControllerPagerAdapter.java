package com.whelch.ledcontroller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.whelch.ledcontroller.fragments.PingPongFragment;
import com.whelch.ledcontroller.fragments.RainbowFragment;

public class BedControllerPagerAdapter extends FragmentStatePagerAdapter {
	
	public BedControllerPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public Fragment getItem(int position) {
		switch(position) {
			case 0: return new RainbowFragment();
			case 1: return new PingPongFragment();
			default: return new RainbowFragment();
		}
	}
	
	@Override
	public int getCount() {
		return 2;
	}
}
