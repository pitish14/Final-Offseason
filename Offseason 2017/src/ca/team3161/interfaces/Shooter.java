package ca.team3161.interfaces;

import ca.team3161.lib.robot.LifecycleListener;

public interface Shooter extends LifecycleListener {
	
	boolean isWheelEnabled();
	
	void toggleWheel();

}
