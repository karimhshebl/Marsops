package Missions;

import assets.resources.Reservation;
import assets.resources.ReserveResult;
import enums.EnvironmentCondition;
import enums.MissionState;
import interfaces.IAgent;
import interfaces.IEnvironmentSensitive;
import interfaces.IResourceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MissionManager {
    private Mission activeMission;
    private Reservation activeReservation;
    private final List<Mission> history = new ArrayList<>();
    private final IResourceManager resources;

    public MissionManager(IResourceManager resources) {
        if (resources == null) throw new IllegalArgumentException("ResourceManager cannot be null");
        this.resources = resources;
    }

    public boolean submit(Mission mission, List<IAgent> agents, EnvironmentCondition env) {
        if (mission == null) throw new IllegalArgumentException("Mission cannot be null");
        if (mission.getState() != MissionState.PENDING) {
            throw new IllegalStateException(mission.getId() + " was already submitted (" + mission.getState() + ")");
        }

        if (activeMission != null) {
            return reject(mission, "Another mission is already active: " + activeMission.getId());
        }

        notifyAgents(agents, env);
        String reason = mission.checkSubmission(agents, env);
        if (reason != null) return reject(mission, reason);

        ReserveResult result = resources.reserve(mission.getId(), mission.getRequirement(), mission.getPriority());
        if (!result.succeeded()) return reject(mission, result.failureReason());

        try {
            mission.assign(agents, env);
        } catch (RuntimeException e) {
            resources.cancel(result.reservation());
            throw e;
        }
        activeMission = mission;
        activeReservation = result.reservation();
        return true;
    }

    public void tick(EnvironmentCondition env) {
        if (activeMission == null) return;

        notifyAgents(activeMission.getAgents(), env);
        activeMission.applyEnvironment(env);

        if (activeMission.getState() == MissionState.ASSIGNED) {
            try {
                resources.commit(activeReservation);
                activeReservation = null;
            } catch (IllegalStateException e) {
                activeMission.fail("Reserved resources are no longer available: " + e.getMessage());
            }
        }

        if (activeMission.isActive()) {
            activeMission.tick();
        }

        if (activeMission.isFinished()) {
            if (activeReservation != null) {
                resources.cancel(activeReservation);
                activeReservation = null;
            }
            history.add(activeMission);
            activeMission = null;
        }
    }

    private boolean reject(Mission mission, String reason) {
        mission.fail(reason);
        history.add(mission);
        return false;
    }

    private void notifyAgents(List<IAgent> agents, EnvironmentCondition env) {
        if (agents == null) return;
        for (IAgent agent : agents) {
            if (agent instanceof IEnvironmentSensitive sensitive) sensitive.applyEnvironment(env);
        }
    }

    public Mission getActiveMission() { return activeMission; }

    public boolean hasActiveMission() { return activeMission != null; }

    public List<Mission> getHistory() { return Collections.unmodifiableList(history); }
}