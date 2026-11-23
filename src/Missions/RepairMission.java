package Missions;

import assets.facilities.Facility;
import assets.resources.ResourceRequirement;
import enums.*;
import interfaces.IAgent;

import java.util.List;

public class RepairMission extends Mission{
    public RepairMission( int duration, List<Facility> targets) {
        super(MissionType.REPAIR, duration, createRequirements(), targets);
    }

    @Override
    protected String getEnvironmentRestriction(EnvironmentCondition environment) {
        return null ;
    }

    @Override
    protected String checkMissionSpecificRules(List<IAgent> candidates, EnvironmentCondition environment) {
        if (getTargets().isEmpty()) return "Repair requires a target facility";
        for (Facility f : getTargets()) {
            if (!f.isDamaged()) return "Target is not damaged: " + f.getName();
        }

        boolean hasHuman = false;
        boolean hasRobot = false;
        for (IAgent agent : candidates) {
            if (!agent.getCapabilities().contains(Capability.REPAIR)) continue;
            if (agent.getKind() == AgentKind.HUMAN) hasHuman = true;
            else if (agent.getKind() == AgentKind.ROBOT) hasRobot = true;
        }

        if (!hasHuman) return "Repair requires an engineer (human with repair capability)";
        if (!hasRobot) return "Repair requires a repair robot (robot with repair capability)";
        return null;
    }

    public static ResourceRequirement createRequirements() {
        return new ResourceRequirement()
                .add(ResourceType.SPARE_PARTS, 2)
                .add(ResourceType.BATTERY_POWER, 5);
    }

    @Override
    public void complete() {
        super.complete();
        for (Facility f : getTargets()) {
            if (f.isDamaged()) f.repair();
        }
    }
}
