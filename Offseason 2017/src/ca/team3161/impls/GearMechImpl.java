package ca.team3161.impls;

import edu.wpi.first.wpilibj.DoubleSolenoid;

public class GearMechImpl implements ca.team3161.interfaces.GearMech {
	
	private DoubleSolenoid claw;
	private DoubleSolenoid flap;

	public GearMechImpl (DoubleSolenoid claw, DoubleSolenoid flap){
	this.claw = claw;
	this.flap = flap;
	flap.set(DoubleSolenoid.Value.kOff);
    claw.set(DoubleSolenoid.Value.kOff);
	}

	@Override
	public void openClaw() {
		claw.set(DoubleSolenoid.Value.kForward);
		
	}

	@Override
	public void closeClaw() {
		 claw.set(DoubleSolenoid.Value.kReverse);
		
	}

	@Override
	public void openFlap() {
		flap.set(DoubleSolenoid.Value.kForward);
		
	}

	@Override
	public void closeFlap() {
		flap.set(DoubleSolenoid.Value.kReverse);
		
	}
}
