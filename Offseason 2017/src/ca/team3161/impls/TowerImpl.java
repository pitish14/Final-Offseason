package ca.team3161.impls;

import ca.team3161.SmartTuner;
import ca.team3161.interfaces.Tower;
import ca.team3161.lib.robot.LifecycleEvent;
import edu.wpi.first.wpilibj.SpeedController;

public class TowerImpl implements Tower {
	
	private double BACK_ELEVATOR_PWM;
	private double FRONT_ELEVATOR_PWM;
	private final SmartTuner backTuner;
	private final SmartTuner frontTuner;
	
	private final SpeedController frontElevator, backElevator;
	
	public TowerImpl(SpeedController frontElevator, SpeedController backElevator) {
		this.frontElevator = frontElevator;
		this.backElevator = backElevator;
		
		this.backTuner = new SmartTuner("BackElevatorPWM", d -> BACK_ELEVATOR_PWM = d, 0.75);
		this.frontTuner = new SmartTuner("FrontElevatorPWM", d -> FRONT_ELEVATOR_PWM = d, 0.9);
	}

	@Override
	public void lifecycleStatusChanged(LifecycleEvent previous, LifecycleEvent current) {
		this.backTuner.lifecycleStatusChanged(previous, current);
		this.frontTuner.lifecycleStatusChanged(previous, current);
	}

	@Override
	public void perform(Action action) {
		switch (action) {
		case INTAKE:
			frontElevator.set(FRONT_ELEVATOR_PWM);
			backElevator.set(-BACK_ELEVATOR_PWM);
			break;
		case NOTHING:
			frontElevator.set(0);
			backElevator.set(0);
			break;
		case OUTTAKE:
			frontElevator.set(-FRONT_ELEVATOR_PWM);
			backElevator.set(BACK_ELEVATOR_PWM);
			break;
		case PUMP:
			frontElevator.set(FRONT_ELEVATOR_PWM);
			backElevator.set(BACK_ELEVATOR_PWM);
			break;
		case UNJAM:
			frontElevator.set(-FRONT_ELEVATOR_PWM);
			backElevator.set(-BACK_ELEVATOR_PWM);
			break;
		default:
			break;
		}
	}

}
