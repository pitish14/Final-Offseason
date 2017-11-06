package ca.team3161.interfaces;

import ca.team3161.lib.robot.LifecycleListener;

public interface Drivetrain extends LifecycleListener {
	
//	void setTractionControlEnabled(boolean enabled);
//	
//	void setDriveRateTarget(double rateTarget);
//	
//	void setDriveTurnTarget(double turnTarget);
	
	void setLeftDrive(double left);
	
	void setRightDrive(double right);
	
}
