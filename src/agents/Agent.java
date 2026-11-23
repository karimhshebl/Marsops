package agents;

import enums.Capability;
import enums.EmergencyType;
import enums.MissionType;
import interfaces.IAgent;
import interfaces.ILocation;
import interfaces.ITimeDependent;
import universalHelpers.Validation;

import java.util.Set;

public abstract class Agent implements IAgent , ITimeDependent {
    private final String id ;
    private final String name;
    private ILocation location;
    private boolean available;
    private final Set<Capability> capabilities;

    public Agent(String id ,String name, ILocation location, Set<Capability> capabilities) {
        this.id = Validation.validateString(id, "ID");
        this.name = Validation.validateString(name,"Name");
        this.location = Validation.validateLocation(location,"agent's location");
        if (capabilities == null) throw new IllegalArgumentException("Capabilities cannot be null");
        this.capabilities = Set.copyOf(capabilities);
        this.available = true;
    }

    @Override
    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    public boolean hasCapability(Capability capability) {
        return capabilities.contains(capability);
    }

    @Override
    public boolean canHandle(EmergencyType emergencyType) {
        return hasCapability(emergencyType.getRequiredCapability());
    }

    @Override
    public boolean canPerform(MissionType missionType) {
        return missionType.getAcceptedCapabilities().stream().anyMatch(this::hasCapability);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ILocation getLocation() {
        return location;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available ;
    }

    @Override
    public void setLocation(ILocation location) {
        Validation.validateLocation(location,"agent's location");
        this.location = location;
    }
}
