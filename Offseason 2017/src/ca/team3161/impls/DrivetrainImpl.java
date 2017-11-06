package ca.team3161.impls;

import ca.team3161.interfaces.Drivetrain;
import ca.team3161.lib.robot.LifecycleEvent;
import ca.team3161.lib.robot.motion.drivetrains.SpeedControllerGroup;
import ca.team3161.lib.robot.pid.RampingSpeedController;
import ca.team3161.lib.robot.pid.TractionController;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.interfaces.Gyro;

public class DrivetrainImpl implements Drivetrain {

	private final SpeedController leftController;
	private final SpeedController rightController;
	private final Encoder leftDriveEncoder, rightDriveEncoder;
	private final Gyro gyro;
	private final RobotDrive drivetrain;

	private volatile double driveRateTarget;
	private volatile double driveTurnTarget;
	
	private volatile double leftDriveTarget;
	private volatile double rightDriveTarget;

	public DrivetrainImpl(SpeedController frontLeftSpeedController, SpeedController backLeftSpeedController,
			SpeedController frontRightSpeedController, SpeedController backRightSpeedController,
			Encoder leftDriveEncoder, Encoder rightDriveEncoder,
			Gyro gyro) {
		this.leftDriveEncoder = leftDriveEncoder;
		this.rightDriveEncoder = rightDriveEncoder;
		
		this.gyro = gyro;

		final double firstFilter = 0.25;
		final double secondFilter = 0.1;
		final double rampRatio = 1.3;
		final double maxStep = 0.025;

		this.leftController = new RampingSpeedController.Builder()
				.controller(new SpeedControllerGroup(frontLeftSpeedController, backLeftSpeedController))
				.maxStep(maxStep)
				.rampRatio(rampRatio)
				.secondFilter(secondFilter)
				.firstFilter(firstFilter)
				.build();
		this.rightController = new RampingSpeedController.Builder()
				.controller(new SpeedControllerGroup(frontRightSpeedController, backRightSpeedController))
				.maxStep(maxStep)
				.rampRatio(rampRatio)
				.secondFilter(secondFilter)
				.firstFilter(firstFilter)
				.build();

		this.drivetrain = new RobotDrive(this.leftController, this.rightController);
	}
	
	@Override
	public void setLeftDrive(double left) {
		this.leftDriveTarget = left;
		updateDriveTargets();
	}
	
	@Override
	public void setRightDrive(double right) {
		this.rightDriveTarget = right;
		updateDriveTargets();
	}
	
	private void resetTargets() {
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		setLeftDrive(0);
		setRightDrive(0);
	}
	
	private void updateDriveTargets() {
		drivetrain.tankDrive(this.leftDriveTarget, this.rightDriveTarget);
	}

	@Override
	public void lifecycleStatusChanged(LifecycleEvent previous, LifecycleEvent current) {
		resetTargets();
	}

}
