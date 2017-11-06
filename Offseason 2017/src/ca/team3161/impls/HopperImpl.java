package ca.team3161.impls;

import ca.team3161.SmartTuner;
import ca.team3161.interfaces.Hopper;
import ca.team3161.lib.robot.LifecycleEvent;
import edu.wpi.first.wpilibj.SpeedController;

public class HopperImpl implements Hopper {
	
	private final SpeedController agitator;
	private final SmartTuner agitatorTuner;
	private static double AGITATOR_PWM;
	
	public HopperImpl(SpeedController agitator) {
		this.agitator = agitator;
		this.agitatorTuner = new SmartTuner("AgitatorPWM", d -> AGITATOR_PWM = d, 0.5);
	}

	@Override
	public void lifecycleStatusChanged(LifecycleEvent previous, LifecycleEvent current) {
		this.agitatorTuner.lifecycleStatusChanged(previous, current);
	}

	@Override
	public void startAgitator(boolean forward) {
		agitator.set(forward ? -AGITATOR_PWM : AGITATOR_PWM);
	}
	
	@Override
	public void stopAgitator() {
		agitator.set(0);
	}
	
}
