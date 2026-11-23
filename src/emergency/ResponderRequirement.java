package emergency;

import enums.AgentKind;
import enums.EmergencyType;
import interfaces.IResponder;

import java.util.ArrayList;
import java.util.List;

public record ResponderRequirement(int minHumans, int minRobots) {
    public ResponderRequirement {
        if (minHumans < 0 || minRobots < 0)
            throw new IllegalArgumentException("Responder counts cannot be negative");
        if (minHumans + minRobots == 0)
            throw new IllegalArgumentException("At least one responder is required");
    }

    public String shortfall(List<? extends IResponder> candidates, EmergencyType type) {
        int humans = eligible(candidates, type, AgentKind.HUMAN).size();
        int robots = eligible(candidates, type, AgentKind.ROBOT).size();
        if (humans < minHumans)
            return "Not enough human responders for " + type + ": need " + minHumans + ", available " + humans;
        if (robots < minRobots)
            return "Not enough robotic responders for " + type + ": need " + minRobots + ", available " + robots;
        return null;
    }

    public List<IResponder> select(List<? extends IResponder> candidates, EmergencyType type) {
        List<IResponder> picked = new ArrayList<>();
        picked.addAll(eligible(candidates, type, AgentKind.HUMAN).subList(0, minHumans));
        picked.addAll(eligible(candidates, type, AgentKind.ROBOT).subList(0, minRobots));
        return picked;
    }

    private static List<IResponder> eligible(List<? extends IResponder> candidates, EmergencyType type, AgentKind kind) {
        List<IResponder> result = new ArrayList<>();
        for (IResponder r : candidates) {
            if (r.getKind() == kind && r.canHandle(type) && r.isAvailable() && r.isUsable()) result.add(r);
        }
        return result;
    }
}