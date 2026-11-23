package simulation;

import colony.MarsColony;
import emergency.Emergency;
import enums.EnvironmentCondition;
import Missions.Mission;
import universalHelpers.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class Simulation {
    private final MarsColony colony;
    private final Consumer<String> log;
    private int time;
    private final Map<Integer, EnvironmentCondition> environmentChanges = new TreeMap<>();
    private final Map<Integer, List<Emergency>> scheduledEmergencies = new TreeMap<>();

    public Simulation(MarsColony colony, Consumer<String> log) {
        if (colony == null) throw new IllegalArgumentException("Colony cannot be null");
        if (log == null) throw new IllegalArgumentException("Log cannot be null");
        this.colony = colony;
        this.log = log;
    }

    public void advanceTime(int units) {
        Validation.validatePositive(units, "Time units");
        for (int i = 0; i < units; i++) step();
    }

    private void step() {
        time++;
        int missionsBefore = colony.getMissionHistory().size();

        EnvironmentCondition change = environmentChanges.remove(time);
        if (change != null) {
            colony.setEnvironment(change);
            log("Environment changed to " + change);
        }

        colony.tick();

        List<Mission> history = colony.getMissionHistory();
        for (int i = missionsBefore; i < history.size(); i++) {
            log("Mission ended: " + history.get(i));
        }

        List<Emergency> due = scheduledEmergencies.remove(time);
        if (due != null) {
            for (Emergency emergency : due) {
                colony.triggerEmergency(emergency);
                log("Emergency handled: " + emergency);
            }
        }
    }

    public void scheduleEnvironmentChange(int atTime, EnvironmentCondition environment) {
        requireFuture(atTime);
        if (environment == null) throw new IllegalArgumentException("Environment cannot be null");
        environmentChanges.put(atTime, environment);
    }

    public void scheduleEmergency(int atTime, Emergency emergency) {
        requireFuture(atTime);
        if (emergency == null) throw new IllegalArgumentException("Emergency cannot be null");
        scheduledEmergencies.computeIfAbsent(atTime, k -> new ArrayList<>()).add(emergency);
    }

    private void requireFuture(int atTime) {
        if (atTime <= time) throw new IllegalArgumentException("Time " + atTime + " is not in the future (now " + time + ")");
    }

    private void log(String message) {
        log.accept("[t=" + time + "] " + message);
    }

    public int getCurrentTime() { return time; }
    public MarsColony getColony() { return colony; }

    public String getStatusReport() {
        return "Time unit: " + time + System.lineSeparator() + colony.getStatusReport();
    }
}
