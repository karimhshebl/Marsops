package agents;

import enums.Capability;
import interfaces.ILocation;

import java.util.Set;

public class MedicalOfficer extends HumanPersonnel {
    public MedicalOfficer(String name, ILocation location) {
        super(name, location, Set.of(Capability.RESCUE, Capability.MEDICAL_CARE, Capability.FIRE_FIGHTING));
    }
}
