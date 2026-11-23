package agents;

import enums.Capability;
import interfaces.ILocation;

import java.util.Set;

public class RepairRobot extends RoboticUnit {
    public RepairRobot(String name, ILocation location) {
        super(name, location, Set.of(Capability.REPAIR, Capability.COMMUNICATION, Capability.FIRE_FIGHTING));
    }
}
