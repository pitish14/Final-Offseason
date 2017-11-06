package ca.team3161;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import ca.team3161.lib.robot.LifecycleEvent;
import ca.team3161.lib.robot.LifecycleListener;
import ca.team3161.lib.robot.subsystem.RepeatingPooledSubsystem;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SmartTuner extends RepeatingPooledSubsystem implements LifecycleListener {
	
	private static final Preferences PREFS;
	static {
		PREFS = Preferences.getInstance();
	}
	
	private final String key;
	private final Consumer<Double> consumer;
	private final double fallback;
	
	public SmartTuner(String key, Consumer<Double> consumer, double fallback) {
		super(500, TimeUnit.MILLISECONDS);
		this.key = key;
		this.consumer = consumer;
		this.fallback = PREFS.getDouble(key, fallback);
		
		SmartDashboard.putNumber(key, this.fallback);
	}

	@Override
	public void defineResources() {
	}

	@Override
	public void task() throws Exception {
		double val = SmartDashboard.getNumber(key, fallback);
		PREFS.putDouble(key, val);
		consumer.accept(val);
	}

	@Override
	public void lifecycleStatusChanged(LifecycleEvent previous, LifecycleEvent current) {
		switch (current) {
		case NONE:
		case ON_INIT:
		case ON_AUTO:
			cancel();
			break;
		case ON_DISABLED:
		case ON_TELEOP:
		case ON_TEST:
			start();
			break;
		default:
			throw new IllegalStateException(current.toString());
		}
	}

}
