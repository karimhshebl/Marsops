package agents;

import enums.Capability;
import enums.CommunicationStatus;
import enums.EnvironmentCondition;
import interfaces.IEnvironmentSensitive;
import interfaces.ILocation;

import java.util.Set;

public class Drone extends RoboticUnit implements IEnvironmentSensitive {
    private CommunicationStatus communicationStatus ;

    public Drone(String name, ILocation location) {
        super(name, location, Set.of(Capability.AERIAL_RECON, Capability.COMMUNICATION, Capability.FIRE_FIGHTING));
        communicationStatus = CommunicationStatus.AVAILABLE;
    }

    @Override
    public boolean isUsable() {
        return (this.getBatteryLevel()>30 )&&(!isDamaged())&&(communicationStatus==CommunicationStatus.AVAILABLE);
    }

    public void setCommunicationStatus(CommunicationStatus communicationStatus){
        this.communicationStatus = communicationStatus;
    }

    public CommunicationStatus getCommunicationStatus() {
        return communicationStatus;
    }

    @Override
    public void applyEnvironment(EnvironmentCondition environment) {
        communicationStatus = (environment == EnvironmentCondition.COMMUNICATION_DISRUPTION)
                ? CommunicationStatus.NOT_AVAILABLE
                : CommunicationStatus.AVAILABLE;
    }
}
