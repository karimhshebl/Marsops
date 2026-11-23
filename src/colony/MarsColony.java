package colony;

import Missions.Mission;
import Missions.MissionManager;
import assets.facilities.Facility;
import assets.resources.ReservationLedger;
import assets.resources.ResourceInventory;
import assets.resources.ResourceManager;
import emergency.Emergency;
import emergency.EmergencyManager;
import enums.EmergencyStatus;
import enums.EnvironmentCondition;
import enums.MissionType;
import enums.ResourceType;
import interfaces.IAgent;
import interfaces.IAllocationPolicy;
import interfaces.IEnvironmentSensitive;
import interfaces.IPerson;
import interfaces.IRobotUnit;
import interfaces.IStockView;
import interfaces.ITimeDependent;
import universalHelpers.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MarsColony implements ITimeDependent {
    private final String name;
    private final ResourceInventory inventory = new ResourceInventory();
    private final ResourceManager resources;
    private final MissionManager missions;
    private final EmergencyManager emergencies;
    private final SurfaceLocation surface = new SurfaceLocation("Martian Surface");
    private final List<Facility> facilities = new ArrayList<>();
    private final List<IAgent> agents = new ArrayList<>();
    private EnvironmentCondition environment = EnvironmentCondition.NORMAL;

    public MarsColony(String name, IAllocationPolicy policy) {
        this.name = Validation.validateString(name, "Colony name");
        if (policy == null) throw new IllegalArgumentException("Allocation policy cannot be null");
        this.resources = new ResourceManager(inventory, new ReservationLedger(), policy);
        this.missions = new MissionManager(resources);
        this.emergencies = new EmergencyManager(resources);
    }

    public void addFacility(Facility facility) {
        if (facility == null) throw new IllegalArgumentException("Facility cannot be null");
        if (facilities.contains(facility)) throw new IllegalArgumentException(facility.getName() + " is already in the colony");
        facilities.add(facility);
    }

    public void addAgent(IAgent agent) {
        if (agent == null) throw new IllegalArgumentException("Agent cannot be null");
        if (agents.contains(agent)) throw new IllegalArgumentException(agent.getName() + " is already in the colony");
        agents.add(agent);
    }

    public boolean submitMission(Mission mission, List<IAgent> assignedAgents) {
        boolean accepted = missions.submit(mission, assignedAgents, environment);
        if (accepted && mission.getType() == MissionType.EXPLORATION) {
            for (IAgent agent : mission.getAgents()) agent.setLocation(surface);
        }
        return accepted;
    }

    public EmergencyStatus triggerEmergency(Emergency emergency) {
        return emergencies.handle(emergency, agents);
    }

    public void setEnvironment(EnvironmentCondition environment) {
        if (environment == null) throw new IllegalArgumentException("Environment cannot be null");
        this.environment = environment;
        notifyEnvironment();
    }

    @Override
    public void tick() {
        notifyEnvironment();
        consumeSupplies();
        for (IAgent agent : agents) agent.tick();
        missions.tick(environment);
    }

    private void notifyEnvironment() {
        for (IAgent agent : agents) {
            if (agent instanceof IEnvironmentSensitive sensitive) sensitive.applyEnvironment(environment);
        }
    }

    private void consumeSupplies() {
        for (ResourceType type : ResourceType.values()) {
            int amount = Math.min(inventory.consumptionPerTick(type), inventory.total(type));
            if (amount > 0) inventory.withdrawAll(Map.of(type, amount));
        }
    }

    public String getName() { return name; }
    public EnvironmentCondition getEnvironment() { return environment; }
    public SurfaceLocation getSurface() { return surface; }
    public ResourceInventory getResourceInventory() { return inventory; }
    public IStockView getStock() { return resources; }
    public List<Facility> getFacilities() { return Collections.unmodifiableList(facilities); }
    public List<IAgent> getAgents() { return Collections.unmodifiableList(agents); }
    public Mission getActiveMission() { return missions.getActiveMission(); }
    public List<Mission> getMissionHistory() { return missions.getHistory(); }
    public List<Emergency> getEmergencyHistory() { return emergencies.getHistory(); }

    public String getStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== COLONY STATUS REPORT: ").append(name).append(" =====").append(System.lineSeparator());
        line(sb, "Environment: " + environment);

        line(sb, "");
        line(sb, "-- Resources (colony-wide, operational facilities only) --");
        for (ResourceType type : ResourceType.values()) {
            int total = resources.total(type);
            int reserved = total - resources.free(type);
            line(sb, String.format("  %-15s total %4d   reserved %3d   use/tick %d",
                    type, total, reserved, resources.consumptionPerTick(type)));
        }

        line(sb, "");
        line(sb, "-- Facilities --");
        for (Facility f : facilities) {
            line(sb, String.format("  %-12s %-10s %-20s %s", f.getId(), f.getName(), f.getClass().getSimpleName(), statusOf(f)));
            String stock = stockOf(f);
            if (!stock.isEmpty()) {
                line(sb, "      stock: " + stock + (f.isInventoryAccessible() ? "" : "  (not counted in colony totals)"));
            }
        }

        line(sb, "");
        line(sb, "-- Agents --");
        for (IAgent a : agents) {
            line(sb, String.format("  %-8s %-10s %-6s at %-16s %-9s %s",
                    a.getId(), a.getName(), a.getKind(), a.getLocation().getName(),
                    a.isAvailable() ? "available" : "on mission", conditionOf(a)));
        }

        line(sb, "");
        line(sb, "-- Missions --");
        Mission active = missions.getActiveMission();
        line(sb, "  Active: " + (active == null ? "none" : active));
        if (missions.getHistory().isEmpty()) line(sb, "  History: none");
        for (Mission m : missions.getHistory()) line(sb, "  " + m);

        line(sb, "");
        line(sb, "-- Emergencies --");
        if (emergencies.getHistory().isEmpty()) line(sb, "  none");
        for (Emergency e : emergencies.getHistory()) line(sb, "  " + e);

        return sb.toString();
    }

    private static void line(StringBuilder sb, String text) {
        sb.append(text).append(System.lineSeparator());
    }

    private static String statusOf(Facility f) {
        if (f.isDestroyed()) return "DESTROYED";
        if (f.isUnavailable()) return "UNAVAILABLE";
        if (f.isDamaged()) return "DAMAGED";
        return "OPERATIONAL";
    }

    private static String stockOf(Facility f) {
        StringBuilder sb = new StringBuilder();
        for (ResourceType type : ResourceType.values()) {
            int quantity = f.getInventory().getQuantity(type);
            if (quantity > 0) sb.append(sb.length() == 0 ? "" : ", ").append(type).append('=').append(quantity);
        }
        return sb.toString();
    }

    private static String conditionOf(IAgent a) {
        if (a instanceof IRobotUnit robot) {
            return "battery " + robot.getBatteryLevel() + (robot.isDamaged() ? ", DAMAGED" : "");
        }
        if (a instanceof IPerson person) {
            return "health " + person.getHealth() + ", fatigue " + person.getFatigue();
        }
        return "";
    }
}
