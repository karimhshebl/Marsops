package interfaces;

import enums.AgentKind;
import enums.Capability;
import enums.EmergencyType;
import enums.MissionType;

import java.util.Set;

public interface IAgent extends IResponder {
    String getId();
    ILocation getLocation();
    void setLocation(ILocation location);
    void setAvailable(boolean available);
    Set<Capability> getCapabilities();
    boolean canPerform(MissionType missionType);
    AgentKind getKind();
    void tick();
}
