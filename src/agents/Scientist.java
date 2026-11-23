package agents;

import enums.Capability;
import interfaces.ILocation;

import java.util.Set;

public class Scientist extends HumanPersonnel {
    public Scientist(String name, ILocation location) {
        super(name, location, Set.of(Capability.RESCUE, Capability.RESEARCH, Capability.SURFACE_EXPLORATION));
    }
}
