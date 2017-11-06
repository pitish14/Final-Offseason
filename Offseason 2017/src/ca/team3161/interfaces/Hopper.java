package ca.team3161.interfaces;

import ca.team3161.lib.robot.LifecycleListener;

public interface Hopper extends LifecycleListener {
	
	void startAgitator(boolean forward);
	
	void stopAgitator();
	
}
