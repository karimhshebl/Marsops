package agents;

import enums.Capability;
import interfaces.ILocation;

import java.util.Set;

public class Engineer extends HumanPersonnel {
    public Engineer(String name, ILocation location) {
        super(name, location, Set.of(Capability.RESCUE, Capability.REPAIR));
    }

}
