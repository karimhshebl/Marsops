package Missions;

import assets.facilities.Facility;
import assets.resources.ResourceRequirement;
import enums.*;
import interfaces.IAgent;
import interfaces.IEquipmentCarrier;
import interfaces.IRobotUnit;

import java.util.List;

public class ExplorationMission extends Mission {
    public static final Equipment REQUIRED_EQUIPMENT = Equipment.SOIL_ANALYZER;

    public ExplorationMission(int duration, List<Facility> targets) {
        super(MissionType.EXPLORATION, duration, createRequirements(), targets);
    }

    @Override
    protected String getEnvironmentRestriction(EnvironmentCondition environment) {
        if (environment == EnvironmentCondition.DUST_STORM)
            return "Cannot operate Exploration Mission in dust storm";
        if (environment == EnvironmentCondition.COMMUNICATION_DISRUPTION)
            return "Cannot operate Exploration Mission during communication disruption";
        return null;
    }

    @Override
    protected String checkMissionSpecificRules(List<IAgent> candidates, EnvironmentCondition environment) {
        boolean hasVehicle = false;
        boolean hasScientist = false;

        for (IAgent agent : candidates) {
            if (!agent.getCapabilities().contains(Capability.SURFACE_EXPLORATION)) continue;

            if (agent.getKind() == AgentKind.ROBOT) {
                if (agent instanceof IRobotUnit robot && !robot.isUsable()) {
                    return "Robot : " + agent.getName() + " is not usable" ;
                }
                hasVehicle = true;
            } else if (agent.getKind() == AgentKind.HUMAN) {
                hasScientist = true;
            }
        }

        if (!hasVehicle)   return "Exploration requires a suitable vehicle (surface-exploration robot)";
        if (!hasScientist) return "Exploration requires personnel with the scientific exploration skill";

        boolean hasEquipment = false;
        for (IAgent agent : candidates) {
            if (agent instanceof IEquipmentCarrier carrier && carrier.getEquipmentCount(REQUIRED_EQUIPMENT) > 0) {
                hasEquipment = true;
            }
        }
        if (!hasEquipment) return "Exploration requires equipment carried by the vehicle: " + REQUIRED_EQUIPMENT;
        return null;
    }

    public static ResourceRequirement createRequirements() {
        return new ResourceRequirement()
                .add(ResourceType.FUEL, 10)
                .add(ResourceType.BATTERY_POWER, 15);
    }
}
