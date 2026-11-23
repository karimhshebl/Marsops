package enums;

import java.util.Set;

public enum MissionType {
    EXPLORATION(Capability.SURFACE_EXPLORATION, Capability.AERIAL_RECON),
    REPAIR(Capability.REPAIR),
    RESEARCH(Capability.RESEARCH),
    RESCUE(Capability.RESCUE);

    private final Set<Capability> acceptedCapabilities;

    MissionType(Capability... acceptedCapabilities) {
        this.acceptedCapabilities = Set.of(acceptedCapabilities);
    }

    public Set<Capability> getAcceptedCapabilities() {
        return acceptedCapabilities;
    }
}
