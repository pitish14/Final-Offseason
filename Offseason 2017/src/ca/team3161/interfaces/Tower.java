package ca.team3161.interfaces;

import ca.team3161.lib.robot.LifecycleListener;

public interface Tower extends LifecycleListener {
	
	public enum Action {
		INTAKE, // ground pickup into hopper
		OUTTAKE, // dump hopper out of front of bot
		PUMP, // hopper or ground up to shooter
		UNJAM, // reverse pump, pull balls away from shooter
		NOTHING,
		;
	}
	
	void perform(Action action);
	
}
