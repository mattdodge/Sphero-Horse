package com.orbotix.horse;

import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.Robot;

public class CollisionListener implements AsyncDataListener {

	private Robot robot;
	private SpheroHorseTwoActivity act;
	
	public CollisionListener(Robot robot, SpheroHorseTwoActivity act) {
		super();
		this.robot = robot;
		this.act = act;
	}
	
	@Override
	public void onDataReceived(DeviceAsyncData data) {
		if (data instanceof CollisionDetectedAsyncData) {
			final CollisionDetectedAsyncData collisionData = (CollisionDetectedAsyncData) data;

//			RGBLEDOutputCommand.sendCommand(robot, (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    		
			act.targetHit();
		}
		
	}

}
