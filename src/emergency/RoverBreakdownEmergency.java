package emergency;

import agents.ExplorationRover;
import assets.resources.ResourceRequirement;
import enums.EmergencyType;
import enums.ResourceType;
import interfaces.ILocation;

public class RoverBreakdownEmergency extends Emergency {
    private final ExplorationRover rover;

    public RoverBreakdownEmergency(ExplorationRover rover) {
        super(EmergencyType.ROVER_BREAKDOWN);
        if (rover == null) throw new IllegalArgumentException("Rover cannot be null");
        this.rover = rover;
    }

    @Override
    public ILocation getLocation() { return rover.getLocation(); }

    @Override
    public ResponderRequirement getResponderRequirement() { return new ResponderRequirement(1, 1); }

    @Override
    public ResourceRequirement getResponseCost() {
        return new ResourceRequirement()
                .add(ResourceType.SPARE_PARTS, 2)
                .add(ResourceType.BATTERY_POWER, 5);
    }

    @Override
    public void onDetected() {
        if (!rover.isDamaged()) rover.damage();
    }

    @Override
    public void onResolved() {
        if (rover.isDamaged()) rover.repair();
    }

    @Override
    public void onUnresolved() {
    }

    public ExplorationRover getRover() { return rover; }
}