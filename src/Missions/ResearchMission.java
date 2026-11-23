package Missions;

import assets.facilities.Facility;
import assets.facilities.Laboratory;
import assets.resources.ResourceRequirement;
import enums.*;
import interfaces.IAgent;

import java.util.List;

public class ResearchMission extends Mission {
    public static final Equipment REQUIRED_EQUIPMENT = Equipment.LABORATORY_APPARATUS;
    public ResearchMission( int duration, List<Facility> targets) {
        super(MissionType.RESEARCH, duration, createRequirements(), targets);
    }

    @Override
    protected String getEnvironmentRestriction(EnvironmentCondition environment) {
        return null;
    }

    @Override
    protected String checkMissionSpecificRules(List<IAgent> candidates, EnvironmentCondition environment) {
        Laboratory lab = null;
        for (Facility f : getTargets()) {
            if (f instanceof Laboratory) { lab = (Laboratory) f; break; }
        }
        if (lab == null) return "Research requires a laboratory";
        if (!lab.isOperational()) return "Laboratory is not operational: " + lab.getName();
        if (lab.getEquipmentCount(REQUIRED_EQUIPMENT) == 0) {
            return "Laboratory " + lab.getName() + " lacks required equipment: " + REQUIRED_EQUIPMENT;
        }

        boolean hasScientist = false;
        for (IAgent agent : candidates) {
            if (agent.getKind() == AgentKind.HUMAN
                    && agent.getCapabilities().contains(Capability.RESEARCH)) {
                hasScientist = true;
            }
        }
        if (!hasScientist) return "Research requires a scientist with the research skill";

        return null;
    }
    @Override
    protected String checkProgressConditions() {
        String reason = super.checkProgressConditions();
        if (reason != null) return reason;
        for (Facility f : getTargets()) {
            if (f instanceof Laboratory && !f.isOperational()) {
                return "Laboratory is no longer operational: " + f.getName();
            }
        }
        return null;
    }

    public static ResourceRequirement createRequirements() {
        return new ResourceRequirement()
                .add(ResourceType.OXYGEN, 10)
                .add(ResourceType.BATTERY_POWER, 20);
    }
}
