package com.orbotix.horse;

import android.util.Log;

public class MissedTimeChecker implements Runnable {

	private SpheroHorseTwoActivity act;
	private int oldTurn, oldPlayer;
	
	public MissedTimeChecker(SpheroHorseTwoActivity act, int oldPlayer, int oldTurn) {
		super();
		this.act = act;
		this.oldPlayer = oldPlayer;
		this.oldTurn = oldTurn;
	}
	
	@Override
	public void run() {
		Log.d("TimeChecker", "Its time! Lets see if they hit the gate yet");
		if (act.getCurrentPlayer() == oldPlayer && act.getCurrentTurn() == oldTurn) {
			act.lose();
		}
	}

}
