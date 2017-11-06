package ca.team3161.impls;

import java.util.concurrent.TimeUnit;

import com.ctre.CANTalon;

import ca.team3161.SmartTuner;
import ca.team3161.interfaces.Shooter;
import ca.team3161.lib.robot.LifecycleEvent;
import ca.team3161.lib.robot.subsystem.RepeatingPooledSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ShooterImpl extends RepeatingPooledSubsystem implements Shooter {

	private double MIN_SHOOTING_SPEED;
	private double SHOOTER_PWM;
	
	private final SmartTuner pwmTuner;
	private final SmartTuner rpmTuner;
	
	private final CANTalon shooterMaster, shooterSlave;
	
	private volatile boolean wheelEnabled = false;

	public ShooterImpl(CANTalon shooterMaster, CANTalon shooterSlave) {
		super(25, TimeUnit.MILLISECONDS);
		this.shooterMaster = shooterMaster;
		this.shooterSlave = shooterSlave;
		
		this.pwmTuner = new SmartTuner("ShooterPWM", d -> SHOOTER_PWM = d, 1);
		this.rpmTuner = new SmartTuner("ShooterRPM", d -> MIN_SHOOTING_SPEED = d, 118_000);
	}
	
	@Override
	public void lifecycleStatusChanged(LifecycleEvent previous, LifecycleEvent current) {
		switch (current) {
		case ON_AUTO:
		case ON_TELEOP:
		case ON_TEST:
			start();
			break;
		case ON_INIT:
		case ON_DISABLED:
			wheelEnabled = false;
		case NONE:
		default:
			cancel();
			break;
		}
		this.pwmTuner.lifecycleStatusChanged(previous, current);
		this.rpmTuner.lifecycleStatusChanged(previous, current);
	}

	@Override
	public void toggleWheel() {
		wheelEnabled = !wheelEnabled;
	}
	
	@Override
	public boolean isWheelEnabled() {
		return wheelEnabled;
	}

	@Override
	public void defineResources() {
		require(shooterMaster);
		require(shooterSlave);
	}

	@Override
	public void task() throws Exception {
		int velocity = shooterMaster.getEncVelocity();
		SmartDashboard.putNumber("ShooterWheelSpeed", velocity);
		shooterMaster.set((velocity < MIN_SHOOTING_SPEED && wheelEnabled) ? SHOOTER_PWM : 0);
//		shooterMaster.set(wheelEnabled ? SHOOTER_PWM : 0);
	}

}
