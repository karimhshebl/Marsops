package interfaces;

import enums.AgentKind;
import enums.EmergencyType;

public interface IResponder {
    public String getName();
    public ILocation getLocation();
    public boolean isAvailable();
    public boolean isUsable();
    public boolean canHandle(EmergencyType emergencyType);
    AgentKind getKind();
    void setLocation(ILocation location);
}
