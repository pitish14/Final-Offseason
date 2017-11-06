package ca.team3161;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import ca.team3161.impls.DrivetrainImpl;
import ca.team3161.impls.GearMechImpl;
import ca.team3161.impls.HopperImpl;
import ca.team3161.impls.ShooterImpl;
import ca.team3161.impls.TowerImpl;
import ca.team3161.interfaces.Drivetrain;
import ca.team3161.interfaces.GearMech;
import ca.team3161.interfaces.Hopper;
import ca.team3161.interfaces.Shooter;
import ca.team3161.interfaces.Tower;
import ca.team3161.interfaces.Tower.Action;
import ca.team3161.lib.robot.LifecycleEvent;
import ca.team3161.lib.utils.controls.DeadbandJoystickMode;
import ca.team3161.lib.utils.controls.Gamepad.Button;
import ca.team3161.lib.utils.controls.Gamepad.PressType;
import ca.team3161.lib.utils.controls.InvertedJoystickMode;
import ca.team3161.lib.utils.controls.JoystickMode;
import ca.team3161.lib.utils.controls.LinearJoystickMode;
import ca.team3161.lib.utils.controls.LogitechDualAction;
import ca.team3161.lib.utils.controls.LogitechDualAction.LogitechAxis;
import ca.team3161.lib.utils.controls.LogitechDualAction.LogitechButton;
import ca.team3161.lib.utils.controls.LogitechDualAction.LogitechControl;
import ca.team3161.lib.utils.controls.SquaredJoystickMode;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Robot extends IterativeRobot {
 
	private static final JoystickMode DEFAULT_driverpad_MODE = new DeadbandJoystickMode(0.05).andThen(new InvertedJoystickMode()).andThen(new SquaredJoystickMode());
	private static final JoystickMode AIMING_driverpad_MODE = new InvertedJoystickMode().andThen(new LinearJoystickMode()).andThen((x) -> x / 2.3);
	
	private SpeedController frontLeftDrive, backLeftDrive, frontRightDrive, backRightDrive;
	private CANTalon frontElevator, backElevator, shooterMaster, shooterSlave;
	private Talon agitator;
	private Encoder leftDriveEncoder, rightDriveEncoder;
	private Gyro gyro;
	private SpeedController climber;
	
	private Drivetrain drivetrain;
	private Hopper hopper;
	private Tower tower;
	private Shooter shooter;
	private GearMech gearmech;
	
	private SendableChooser<AutoMode> autoModeChooser;
	private Timer autoTimer;
	private AutoMode selectedAutoMode;
	
	private SmartTuner gearAutoPwmTuner;
	private SmartTuner gearAutoDriveTimeTuner;
	
	private static volatile double GEAR_AUTO_PWM;
	private static volatile double GEAR_AUTO_DRIVE_TIME;
	
	private LogitechDualAction driverpad;
	
	private enum AutoMode {
		DO_NOTHING,
		CENTER_GEAR,
		DRIVE_HALF,
		;
	}

	@Override
	public void robotInit() {
		frontLeftDrive = new VictorSP(3);
		backLeftDrive = new VictorSP(4);
		frontRightDrive = new VictorSP(1);
		backRightDrive = new VictorSP(2);
		
		frontElevator = new CANTalon(2);
		backElevator = new CANTalon(5);
		shooterMaster = new CANTalon(3);
		shooterSlave = new CANTalon(4);
		shooterSlave.changeControlMode(TalonControlMode.Follower);
		shooterSlave.set(shooterMaster.getDeviceID());
		
		agitator = new Talon(9);
		
		climber = new Talon(8);
		
		leftDriveEncoder = new Encoder(3, 4);
		rightDriveEncoder = new Encoder(1, 2);
		
		DoubleSolenoid claw = new DoubleSolenoid (1,2);
		DoubleSolenoid flap = new DoubleSolenoid (3,4);
		
		gyro = new ADXRS450_Gyro();
		
		drivetrain = new DrivetrainImpl(frontLeftDrive, backLeftDrive, frontRightDrive, backRightDrive,
				leftDriveEncoder, rightDriveEncoder, gyro);
		hopper = new HopperImpl(agitator);
		tower = new TowerImpl(frontElevator, backElevator);
		shooter = new ShooterImpl(shooterMaster, shooterSlave);
		gearmech = new GearMechImpl();

		autoModeChooser = new SendableChooser<>();
		EnumSet.complementOf(EnumSet.of(AutoMode.DO_NOTHING)).forEach(mode -> autoModeChooser.addObject(mode.toString(), mode));
		autoModeChooser.addDefault(AutoMode.DO_NOTHING.toString(), AutoMode.DO_NOTHING);
		SmartDashboard.putData("AutoModeChooser", autoModeChooser);
		autoTimer = new Timer();
		
		  LogitechDualAction driverpad = new LogitechDualAction(0, 50, TimeUnit.MILLISECONDS);
	        registerLifecycleComponent(driverpad);

	       LogitechDualAction operatorpad = new LogitechDualAction(1, 50, TimeUnit.MILLISECONDS);
	       registerLifecycleComponent(operatorpad);

	      
		driverpad.map(LogitechControl.LEFT_STICK, LogitechAxis.Y, drivetrain::setLeftDrive);
		driverpad.map(LogitechControl.RIGHT_STICK, LogitechAxis.Y, drivetrain::setRightDrive);
	
		driverpad.bind(LogitechButton.LEFT_TRIGGER, PressType.PRESS, () -> climber.set(-1));
		driverpad.bind(LogitechButton.LEFT_TRIGGER, PressType.RELEASE, () -> climber.set(0));
		driverpad.bind(LogitechButton.LEFT_BUMPER, PressType.PRESS, () -> climber.set(-0.5));
		driverpad.bind(LogitechButton.LEFT_BUMPER, PressType.RELEASE, () -> climber.set(0));
		Set<Button> climberCombo = new HashSet<>();
		climberCombo.add((Button) LogitechButton.LEFT_TRIGGER);
		climberCombo.add((Button) LogitechButton.LEFT_BUMPER);
		driverpad.bind(climberCombo, PressType.PRESS, () -> climber.set(-1));
		driverpad.bind(climberCombo, PressType.RELEASE, () -> climber.set(0));
		
		operatorpad.bind(LogitechButton.X, PressType.PRESS, gearmech::openClaw);
		operatorpad.bind(LogitechButton.X, PressType.RELEASE, gearmech::closeClaw);
		operatorpad.bind(LogitechButton.B, PressType.PRESS, gearmech::openFlap);
		operatorpad.bind(LogitechButton.B, PressType.RELEASE, gearmech::closeFlap);
		
		
		driverpad.bind(LogitechButton.RIGHT_TRIGGER, b -> {
			if (b) {
				if (!shooter.isWheelEnabled()) {
					shooter.toggleWheel();
				}
				hopper.startAgitator(true);
				tower.perform(Action.PUMP);
			} else {
				hopper.stopAgitator();
				tower.perform(Action.NOTHING);
			}
		});
		driverpad.bind(LogitechButton.RIGHT_BUMPER, b -> {
				tower.perform(b ? Action.INTAKE : Action.NOTHING);
				if (b) {
					hopper.startAgitator(false);
				} else {
					hopper.stopAgitator();
				}
			}
		);
		
		driverpad.bind(LogitechButton.A, shooter::toggleWheel);
		driverpad.bind(LogitechButton.X, () -> {});
		driverpad.bind(LogitechButton.B, b -> {
			if (b) {
				tower.perform(Action.OUTTAKE);
				hopper.startAgitator(true);
			} else {
				tower.perform(Action.NOTHING);
				hopper.stopAgitator();
			}
		});
		driverpad.bind(LogitechButton.Y, b -> tower.perform(b ? Action.UNJAM : Action.NOTHING));
		
		gearAutoPwmTuner = new SmartTuner("GearAutoPWM", d -> GEAR_AUTO_PWM = d, -0.55);
		gearAutoDriveTimeTuner = new SmartTuner("GearAutoTime", d -> GEAR_AUTO_DRIVE_TIME = d, 3.2);
		
		try {
			Thread thread = new Thread(() -> {
				final UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if (camera != null) {
						camera.free();
						CameraServer.getInstance().removeCamera(camera.getName());
					}
				}));
			});
			thread.setDaemon(true);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
			SmartDashboard.putString("ExceptionLog", e.getMessage());
		}
		
		gyro.calibrate(); // SYNCHRONOUS, TAKES ~8-10 SECONDS !!!!
	}
	
	private void registerLifecycleComponent(LogitechDualAction operatorpad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void autonomousInit() {
		hopper.lifecycleStatusChanged(null, LifecycleEvent.ON_AUTO);
		shooter.lifecycleStatusChanged(null, LifecycleEvent.ON_AUTO);
		tower.lifecycleStatusChanged(null, LifecycleEvent.ON_AUTO);
		driverpad.lifecycleStatusChanged(null, LifecycleEvent.ON_AUTO);
		gearAutoPwmTuner.lifecycleStatusChanged(null, LifecycleEvent.ON_AUTO);
		gearAutoDriveTimeTuner.lifecycleStatusChanged(null, LifecycleEvent.ON_AUTO);
		
		selectedAutoMode = autoModeChooser.getSelected();
		autoTimer.reset();
		autoTimer.start();
	}

	@Override
	public void autonomousPeriodic() {
		switch (selectedAutoMode) {
		case DO_NOTHING:
			SmartDashboard.putString("AUTO", "Twiddling thumbs...");
			break;
		case CENTER_GEAR:
			SmartDashboard.putString("AUTO", "Placing center gear");
			if (autoTimer.get() < GEAR_AUTO_DRIVE_TIME) {
				drivetrain.setLeftDrive(GEAR_AUTO_PWM);
				drivetrain.setRightDrive(GEAR_AUTO_PWM);
			} else {
				drivetrain.setLeftDrive(0);
				drivetrain.setRightDrive(0);
			}
			break;
		case DRIVE_HALF:
			if (autoTimer.get() < 1.2) {
				SmartDashboard.putString("AUTO", "DRIVING");
				drivetrain.setLeftDrive(1);
				drivetrain.setRightDrive(1);
			} else {
				SmartDashboard.putString("AUTO", "STOPPING");
				drivetrain.setLeftDrive(0);
				drivetrain.setRightDrive(0);
			}
			break;
		default:
			SmartDashboard.putString("AUTO", "Unknown mode: " + selectedAutoMode.toString());
			break;
		}
	}

	@Override
	public void disabledInit() {
		hopper.lifecycleStatusChanged(null, LifecycleEvent.ON_DISABLED);
		shooter.lifecycleStatusChanged(null, LifecycleEvent.ON_DISABLED);
		tower.lifecycleStatusChanged(null, LifecycleEvent.ON_DISABLED);
		driverpad.lifecycleStatusChanged(null, LifecycleEvent.ON_DISABLED);
		gearAutoPwmTuner.lifecycleStatusChanged(null, LifecycleEvent.ON_DISABLED);
		gearAutoDriveTimeTuner.lifecycleStatusChanged(null, LifecycleEvent.ON_DISABLED);
	}

	@Override
	public void disabledPeriodic() {
		SmartDashboard.putNumber("GyroHeading", gyro.getAngle());
	}

	@Override
	public void teleopInit() {
		hopper.lifecycleStatusChanged(null, LifecycleEvent.ON_TELEOP);
		shooter.lifecycleStatusChanged(null, LifecycleEvent.ON_TELEOP);
		tower.lifecycleStatusChanged(null, LifecycleEvent.ON_TELEOP);
		driverpad.lifecycleStatusChanged(null, LifecycleEvent.ON_TELEOP);
		gearAutoPwmTuner.lifecycleStatusChanged(null, LifecycleEvent.ON_TELEOP);
		gearAutoDriveTimeTuner.lifecycleStatusChanged(null, LifecycleEvent.ON_TELEOP);
	}

	@Override
	public void teleopPeriodic() {
	}

	public static JoystickMode getDefaultDriverpadMode() {
		return DEFAULT_driverpad_MODE;
	}

	public static JoystickMode getAimingDriverpadMode() {
		return AIMING_driverpad_MODE;
	}

}
