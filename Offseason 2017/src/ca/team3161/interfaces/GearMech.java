package ca.team3161.interfaces;

import ca.team3161.lib.robot.LifecycleListener;

public interface GearMech extends LifecycleListener {

		void openclaw();
		
		void closeclaw();
		
		void openflap();
		
		void closeflap();
	}

