package ca.team3161.impls;

import edu.wpi.first.wpilibj.DoubleSolenoid;

/** This is for the 2017 code package. That code is not on Github
 * I randomly created a package and when i know what the package name is, 
 * i will fix it then
 */

public class GearMechImpl {
	
	private DoubleSolenoid claw;
	private DoubleSolenoid flap;

	public void GearMech (DoubleSolenoid claw, DoubleSolenoid flap){
	this.claw = claw;
	this.flap = flap;
	flap.set(DoubleSolenoid.Value.kOff);
    claw.set(DoubleSolenoid.Value.kOff);
	}

	/**Classifying where the solenoids will be in PCM
    */
	
    /**
     * Release the flap
     */
	public void pullflap() {
        flap.set(DoubleSolenoid.Value.kForward);
    }

    /**
     * Set the Flap back in
     */
    public void returnflap() {
        flap.set(DoubleSolenoid.Value.kReverse);
    }

    /**
     * Open the Claw
     */
    public void openclaw() {
        claw.set(DoubleSolenoid.Value.kForward);
    }

    /**
     * Close the Claw
     */
    public void closeclaw() {
        claw.set(DoubleSolenoid.Value.kReverse);
    }
}
