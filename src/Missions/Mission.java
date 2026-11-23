package Missions;
import assets.facilities.Facility;
import assets.resources.ResourceRequirement;
import enums.EnvironmentCondition;
import enums.MissionState;
import enums.MissionType;
import enums.RequestPriority;
import interfaces.IAgent;
import interfaces.IMission;
import universalHelpers.IdGenerator;
import universalHelpers.Validation;

import java.util.Collections;
import java.util.List;

public abstract class Mission implements IMission {
    private final String id;
    private final MissionType type;
    private final int duration;
    private int elapsed;
    private MissionState state;
    private final ResourceRequirement requirement;
    private final List<Facility> targets;
    private List<IAgent> agents = List.of();
    private String failureReason;

    public Mission(MissionType type, int duration, ResourceRequirement requirement, List<Facility> targets) {
        if (type == null) throw new IllegalArgumentException("Mission type cannot be null");
        if (requirement == null) throw new IllegalArgumentException("Resource requirement cannot be null");

        this.id = IdGenerator.generateId("MIS-");

        this.type = type;
        this.duration = Validation.validatePositive(duration, "Mission duration");
        this.requirement = requirement;
        this.targets = (targets == null) ? List.of() : List.copyOf(targets);
        this.elapsed = 0;
        this.state = MissionState.PENDING;
    }

    public final String checkSubmission(List<IAgent> candidates, EnvironmentCondition environment) {
        if (state != MissionState.PENDING) {
            return "Mission has already been submitted (state: " + state + ")";
        }
        if (candidates == null || candidates.isEmpty()) {
            return "No agents were provided for the mission";
        }

        String environmentReason = getEnvironmentRestriction(environment);
        if (environmentReason != null) return environmentReason;

        for (IAgent agent : candidates) {
            if (agent == null) return "Agent list contains a null agent";
            if (!agent.canPerform(type)) {
                return "Agent does not possess the required capability: " + agent.getName();
            }
            if (!agent.isAvailable()) {
                return "Agent is not available: " + agent.getName();
            }
            if (!agent.isUsable()) {
                return "Agent is not in usable condition: " + agent.getName();
            }
        }

        return checkMissionSpecificRules(candidates, environment);
    }

    public void assign(List<IAgent> assignedAgents, EnvironmentCondition environment) {
        String reason = checkSubmission(assignedAgents, environment);
        if (reason != null) {
            throw new IllegalStateException("Cannot assign " + id + ": " + reason);
        }
        this.agents = List.copyOf(assignedAgents);
        for (IAgent agent : agents) {
            agent.setAvailable(false);
        }
        this.state = MissionState.ASSIGNED;
    }
    public void start() {
        requireState(MissionState.ASSIGNED, "start");
        this.state = MissionState.IN_PROGRESS;
    }

    public void complete() {
        requireState(MissionState.IN_PROGRESS, "complete");
        this.state = MissionState.COMPLETED;
        releaseAgents();
    }

    public void fail(String reason) {
        if (isFinished()) {
            throw new IllegalStateException(id + " has already ended (" + state + ") and cannot fail");
        }
        this.failureReason = Validation.validateString(reason, "Failure reason");
        this.state = MissionState.FAILED;
        releaseAgents();
    }
    @Override
    public void tick() {
        if (state == MissionState.ASSIGNED) {
            start();
        }
        if (state == MissionState.IN_PROGRESS) {
            String reason = checkProgressConditions();
            if (reason != null) {
                fail(reason);
                return;
            }
            elapsed++;
            if (elapsed >= duration) {
                complete();
            }
        }
    }

    public void applyEnvironment(EnvironmentCondition environment) {
        if (state == MissionState.ASSIGNED || state == MissionState.IN_PROGRESS) {
            String reason = getEnvironmentRestriction(environment);
            if (reason != null) fail(reason);
        }
    }

    public String getId() { return id; }
    public MissionType getType() { return type; }
    public MissionState getState() { return state; }

    public RequestPriority getPriority() { return RequestPriority.ROUTINE; }
    public int getDuration() { return duration; }
    public int getElapsed() { return elapsed; }
    public int getRemaining() { return duration - elapsed; }
    public ResourceRequirement getRequirement() { return requirement; }
    public List<Facility> getTargets() { return Collections.unmodifiableList(targets); }
    public List<IAgent> getAgents() { return Collections.unmodifiableList(agents); }
    public String getFailureReason() { return failureReason; }

    public boolean isActive() {
        return state == MissionState.ASSIGNED || state == MissionState.IN_PROGRESS;
    }

    public boolean isFinished() {
        return state == MissionState.COMPLETED || state == MissionState.FAILED;
    }

    @Override
    public String toString() {
        String text = id + " [" + type + ", " + state + ", " + elapsed + "/" + duration + "]";
        if (failureReason != null) text += " reason: " + failureReason;
        return text;
    }

    protected abstract String getEnvironmentRestriction(EnvironmentCondition environment) ;
    //return null if acceptable

    protected abstract String checkMissionSpecificRules(List<IAgent> candidates, EnvironmentCondition environment) ;
    //return null if acceptable

    protected String checkProgressConditions() {
        for (IAgent agent : agents) {
            if (!agent.isUsable()) {
                return "Agent is no longer usable (insufficient battery, damage or health): " + agent.getName();
            }
        }
        return null;
    }

    private void releaseAgents() {
        for (IAgent agent : agents) {
            agent.setAvailable(true);
        }
    }

    private void requireState(MissionState expected, String action) {
        if (state != expected) {
            throw new IllegalStateException("Cannot " + action + " " + id + ": expected state " + expected + " but was " + state);
        }
    }

}