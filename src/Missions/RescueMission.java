package Missions;

import assets.facilities.Facility;
import assets.resources.ResourceRequirement;
import enums.*;
import interfaces.IAgent;

import java.util.List;

public class RescueMission extends Mission{
    public RescueMission( int duration, List<Facility> targets) {
        super(MissionType.RESCUE, duration, createRequirements(), targets);
    }

    @Override
    public RequestPriority getPriority() {
        return RequestPriority.LIFE_CRITICAL;
    }

    @Override
    protected String getEnvironmentRestriction(EnvironmentCondition environment) {
        if (environment == EnvironmentCondition.DUST_STORM)
            return "Cannot operate Rescue Mission in dust storm";
        if (environment == EnvironmentCondition.COMMUNICATION_DISRUPTION)
            return "Cannot operate Rescue Mission during communication disruption";
        return null;
    }

    @Override
    protected String checkMissionSpecificRules(List<IAgent> candidates, EnvironmentCondition environment) {
        boolean hasVehicle = false;
        boolean hasPersonnel = false;
        boolean hasCommunication = false;

        for (IAgent agent : candidates) {
            if (agent.getKind() == AgentKind.ROBOT
                    && agent.getCapabilities().contains(Capability.SURFACE_EXPLORATION)) {
                hasVehicle = true;
            }
            if (agent.getKind() == AgentKind.HUMAN) {
                hasPersonnel = true;
            }
            if (agent.getCapabilities().contains(Capability.COMMUNICATION)) {
                hasCommunication = true;
            }
        }

        if (!hasVehicle)       return "Rescue requires a suitable vehicle (surface-capable robot)";
        if (!hasPersonnel)     return "Rescue requires available personnel";
        if (!hasCommunication) return "Rescue requires an agent with communication capability";
        return null;
    }

    public static ResourceRequirement createRequirements() {
        return new ResourceRequirement()
                .add(ResourceType.FUEL, 15)
                .add(ResourceType.OXYGEN, 10)
                .add(ResourceType.MEDICAL_SUPPLY, 2);
    }
}
