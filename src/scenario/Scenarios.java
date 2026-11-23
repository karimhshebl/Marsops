package scenario;

import Missions.ExplorationMission;
import Missions.Mission;
import agents.Drone;
import agents.Engineer;
import agents.ExplorationRover;
import agents.RepairRobot;
import agents.Scientist;
import assets.facilities.Habitat;
import assets.facilities.Laboratory;
import assets.facilities.StorageFacility;
import assets.resources.ResourceInventory;
import assets.resources.TieredAllocationPolicy;
import colony.MarsColony;
import emergency.OxygenLeakEmergency;
import emergency.RoverBreakdownEmergency;
import enums.EnvironmentCondition;
import enums.Equipment;
import enums.ResourceType;
import interfaces.IAgent;
import simulation.Simulation;
import universalHelpers.IdGenerator;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Scenarios {
    private Scenarios() { }

    public record World(MarsColony colony, Simulation simulation,
                        Habitat habitat, Laboratory laboratory, StorageFacility storage) { }

    public record Scenario(String id, String title, String description, Function<Consumer<String>, World> runner) {
        public World run(Consumer<String> out) { return runner.apply(out); }

        @Override
        public String toString() { return title; }
    }

    public static List<Scenario> all() {
        return List.of(
                new Scenario("1", "Scenario 1: Successful exploration",
                        "A scientist and an exploration rover with a soil analyzer run a 3 unit exploration mission. "
                                + "The colony status report is printed before and after.",
                        Scenarios::scenario1SuccessfulExploration),
                new Scenario("2", "Scenario 2: Mission rejection",
                        "An engineer is submitted to an exploration mission. The mission fails immediately with a reason "
                                + "and never becomes active.",
                        Scenarios::scenario2MissionRejection),
                new Scenario("3", "Scenario 3: Resource shortage",
                        "Only 5 fuel exists but the mission needs 10. The mission is rejected and no resources are consumed.",
                        Scenarios::scenario3ResourceShortage),
                new Scenario("4a", "Scenario 4a: Oxygen leak, resolved",
                        "A 30 unit oxygen leak in the habitat. An engineer and a repair robot are dispatched, resources "
                                + "are consumed and the habitat is repaired.",
                        Scenarios::scenario4aOxygenLeakResolved),
                new Scenario("4b", "Scenario 4b: Oxygen leak, unresolved",
                        "The same leak with no repair robot. The emergency is UNRESOLVED with a reason, the oxygen is "
                                + "deducted twice and nothing is consumed.",
                        Scenarios::scenario4bOxygenLeakUnresolved),
                new Scenario("5a", "Scenario 5a: Dust storm blocks a mission",
                        "An exploration mission is submitted during a dust storm and is rejected.",
                        Scenarios::scenario5aDustStormBlocksMission),
                new Scenario("5b", "Scenario 5b: Dust storm hits a running mission",
                        "A dust storm starts at time 2 while a 5 unit exploration mission is running. The mission fails.",
                        Scenarios::scenario5bDustStormFailsActiveMission),
                new Scenario("5c", "Scenario 5c: Communication disruption",
                        "A communication disruption cuts off a drone. It is usable again when conditions return to normal.",
                        Scenarios::scenario5cCommunicationDisruption),
                new Scenario("6", "Extra: Rover breakdown during a mission",
                        "A rover breaks down during an exploration mission and no repair responders exist. "
                                + "The emergency is unresolved and the mission fails on the next time unit.",
                        Scenarios::extraRoverBreakdown));
    }

    public static World buildWorld(int fuel, Consumer<String> out) {
        IdGenerator.reset();
        MarsColony colony = new MarsColony("Ares Base", new TieredAllocationPolicy());
        ResourceInventory inventory = colony.getResourceInventory();

        Habitat habitat = new Habitat("Hab-A", inventory);
        Laboratory laboratory = new Laboratory("Lab-1", inventory);
        StorageFacility storage = new StorageFacility("Storage-1", inventory);
        colony.addFacility(habitat);
        colony.addFacility(laboratory);
        colony.addFacility(storage);

        habitat.getInventory().addResource(ResourceType.OXYGEN, 300);
        habitat.getInventory().addResource(ResourceType.WATER, 100);
        habitat.getInventory().addResource(ResourceType.FOOD, 100);
        storage.getInventory().addResource(ResourceType.OXYGEN, 200);
        storage.getInventory().addResource(ResourceType.BATTERY_POWER, 60);
        storage.getInventory().addResource(ResourceType.SPARE_PARTS, 10);
        storage.getInventory().addResource(ResourceType.MEDICAL_SUPPLY, 10);
        storage.getInventory().addResource(ResourceType.FUEL, fuel);

        inventory.setConsumptionPerTick(ResourceType.OXYGEN, 2);
        inventory.setConsumptionPerTick(ResourceType.WATER, 1);
        inventory.setConsumptionPerTick(ResourceType.FOOD, 1);

        Simulation simulation = new Simulation(colony, out);
        return new World(colony, simulation, habitat, laboratory, storage);
    }

    private static World scenario1SuccessfulExploration(Consumer<String> out) {
        heading(out, "Scenario 1: Successful Exploration");
        World w = buildWorld(50, out);
        Scientist scientist = new Scientist("Dr. Nour", w.laboratory());
        ExplorationRover rover = new ExplorationRover("Rover-1", w.storage());
        rover.addEquipment(Equipment.SOIL_ANALYZER, 1);
        w.colony().addAgent(scientist);
        w.colony().addAgent(rover);

        out.accept("--- State before ---");
        text(out, w.simulation().getStatusReport());

        Mission mission = new ExplorationMission(3, List.of());
        submit(out, w, mission, List.of(scientist, rover));
        w.simulation().advanceTime(3);

        out.accept("--- State after ---");
        text(out, w.simulation().getStatusReport());
        return w;
    }

    private static World scenario2MissionRejection(Consumer<String> out) {
        heading(out, "Scenario 2: Mission Rejection (unsuitable agent)");
        World w = buildWorld(50, out);
        Engineer engineer = new Engineer("Eng. Omar", w.habitat());
        ExplorationRover rover = new ExplorationRover("Rover-1", w.storage());
        rover.addEquipment(Equipment.SOIL_ANALYZER, 1);
        w.colony().addAgent(engineer);
        w.colony().addAgent(rover);

        Mission mission = new ExplorationMission(3, List.of());
        submit(out, w, mission, List.of(engineer, rover));
        out.accept("Mission state: " + mission.getState());
        out.accept("Active mission: " + describeActive(w));
        out.accept("Engineer available: " + engineer.isAvailable() + ", rover available: " + rover.isAvailable());
        return w;
    }

    private static World scenario3ResourceShortage(Consumer<String> out) {
        heading(out, "Scenario 3: Resource Shortage");
        World w = buildWorld(5, out);
        Scientist scientist = new Scientist("Dr. Nour", w.laboratory());
        ExplorationRover rover = new ExplorationRover("Rover-1", w.storage());
        rover.addEquipment(Equipment.SOIL_ANALYZER, 1);
        w.colony().addAgent(scientist);
        w.colony().addAgent(rover);

        printResources(out, w, "Resources before", ResourceType.FUEL, ResourceType.BATTERY_POWER);
        Mission mission = new ExplorationMission(3, List.of());
        submit(out, w, mission, List.of(scientist, rover));
        out.accept("Mission state: " + mission.getState());
        printResources(out, w, "Resources after ", ResourceType.FUEL, ResourceType.BATTERY_POWER);
        return w;
    }

    private static World scenario4aOxygenLeakResolved(Consumer<String> out) {
        heading(out, "Scenario 4a: Oxygen Leak, resolved");
        World w = buildWorld(50, out);
        Engineer engineer = new Engineer("Eng. Omar", w.laboratory());
        RepairRobot robot = new RepairRobot("Fixer-1", w.storage());
        w.colony().addAgent(engineer);
        w.colony().addAgent(robot);

        out.accept("Engineer at " + engineer.getLocation().getName() + ", robot at " + robot.getLocation().getName());
        printOxygenLeakState(out, w, "Before");
        OxygenLeakEmergency leak = new OxygenLeakEmergency(w.habitat(), 30);
        w.colony().triggerEmergency(leak);
        out.accept("Emergency: " + leak);
        out.accept("Engineer now at " + engineer.getLocation().getName() + ", robot now at " + robot.getLocation().getName());
        printOxygenLeakState(out, w, "After ");
        out.accept("Total oxygen lost to the leak: " + leak.getTotalOxygenLost());
        return w;
    }

    private static World scenario4bOxygenLeakUnresolved(Consumer<String> out) {
        heading(out, "Scenario 4b: Oxygen Leak, unresolved (no repair robot)");
        World w = buildWorld(50, out);
        Engineer engineer = new Engineer("Eng. Omar", w.laboratory());
        w.colony().addAgent(engineer);

        printOxygenLeakState(out, w, "Before");
        OxygenLeakEmergency leak = new OxygenLeakEmergency(w.habitat(), 30);
        w.colony().triggerEmergency(leak);
        out.accept("Emergency: " + leak);
        printOxygenLeakState(out, w, "After ");
        out.accept("Total oxygen lost to the leak: " + leak.getTotalOxygenLost() + " (twice the leak size)");
        return w;
    }

    private static World scenario5aDustStormBlocksMission(Consumer<String> out) {
        heading(out, "Scenario 5a: Dust storm blocks an exploration mission");
        World w = buildWorld(50, out);
        Scientist scientist = new Scientist("Dr. Nour", w.laboratory());
        ExplorationRover rover = new ExplorationRover("Rover-1", w.storage());
        rover.addEquipment(Equipment.SOIL_ANALYZER, 1);
        w.colony().addAgent(scientist);
        w.colony().addAgent(rover);

        w.colony().setEnvironment(EnvironmentCondition.DUST_STORM);
        out.accept("Environment: " + w.colony().getEnvironment());
        Mission mission = new ExplorationMission(3, List.of());
        submit(out, w, mission, List.of(scientist, rover));
        out.accept("Active mission: " + describeActive(w));
        return w;
    }

    private static World scenario5bDustStormFailsActiveMission(Consumer<String> out) {
        heading(out, "Scenario 5b: Dust storm hits while a mission is running");
        World w = buildWorld(50, out);
        Scientist scientist = new Scientist("Dr. Nour", w.laboratory());
        ExplorationRover rover = new ExplorationRover("Rover-1", w.storage());
        rover.addEquipment(Equipment.SOIL_ANALYZER, 1);
        w.colony().addAgent(scientist);
        w.colony().addAgent(rover);

        Mission mission = new ExplorationMission(5, List.of());
        w.simulation().scheduleEnvironmentChange(2, EnvironmentCondition.DUST_STORM);
        submit(out, w, mission, List.of(scientist, rover));
        w.simulation().advanceTime(3);
        out.accept("Mission: " + mission);
        out.accept("Active mission: " + describeActive(w));
        out.accept("Scientist available: " + scientist.isAvailable() + ", rover available: " + rover.isAvailable()
                + ", rover at: " + rover.getLocation().getName());
        return w;
    }

    private static World scenario5cCommunicationDisruption(Consumer<String> out) {
        heading(out, "Scenario 5c: Communication disruption cuts off a drone");
        World w = buildWorld(50, out);
        Drone drone = new Drone("Drone-1", w.habitat());
        w.colony().addAgent(drone);

        out.accept("Drone usable (normal): " + drone.isUsable());
        w.colony().setEnvironment(EnvironmentCondition.COMMUNICATION_DISRUPTION);
        out.accept("Drone usable (communication disruption): " + drone.isUsable()
                + ", link: " + drone.getCommunicationStatus());
        w.colony().setEnvironment(EnvironmentCondition.NORMAL);
        out.accept("Drone usable (back to normal): " + drone.isUsable());
        return w;
    }

    private static World extraRoverBreakdown(Consumer<String> out) {
        heading(out, "Extra: Rover breakdown during an exploration mission (no repair responders)");
        World w = buildWorld(50, out);
        Scientist scientist = new Scientist("Dr. Nour", w.laboratory());
        ExplorationRover rover = new ExplorationRover("Rover-1", w.storage());
        rover.addEquipment(Equipment.SOIL_ANALYZER, 1);
        w.colony().addAgent(scientist);
        w.colony().addAgent(rover);

        Mission mission = new ExplorationMission(5, List.of());
        submit(out, w, mission, List.of(scientist, rover));
        w.simulation().advanceTime(1);

        RoverBreakdownEmergency breakdown = new RoverBreakdownEmergency(rover);
        w.colony().triggerEmergency(breakdown);
        out.accept("Emergency: " + breakdown);
        out.accept("Rover damaged: " + rover.isDamaged() + ", rover at: " + rover.getLocation().getName());

        w.simulation().advanceTime(1);
        out.accept("Mission: " + mission);
        out.accept("Scientist available again: " + scientist.isAvailable());
        return w;
    }

    private static void submit(Consumer<String> out, World w, Mission mission, List<IAgent> team) {
        boolean accepted = w.colony().submitMission(mission, team);
        if (accepted) {
            out.accept("Mission " + mission.getId() + " (" + mission.getType() + ") accepted: " + mission.getState());
        } else {
            out.accept(mission.getType() + " mission cannot be assigned.");
            out.accept("Reason: " + mission.getFailureReason());
        }
    }

    private static String describeActive(World w) {
        Mission active = w.colony().getActiveMission();
        return active == null ? "none" : active.toString();
    }

    private static void printResources(Consumer<String> out, World w, String label, ResourceType... types) {
        StringBuilder sb = new StringBuilder(label).append(": ");
        for (ResourceType type : types) {
            sb.append(type).append('=').append(w.colony().getStock().total(type)).append("  ");
        }
        out.accept(sb.toString().trim());
    }

    private static void printOxygenLeakState(Consumer<String> out, World w, String label) {
        out.accept(label + ": habitat oxygen=" + w.habitat().getInventory().getQuantity(ResourceType.OXYGEN)
                + ", habitat " + (w.habitat().isDamaged() ? "DAMAGED" : "OPERATIONAL")
                + ", colony oxygen=" + w.colony().getStock().total(ResourceType.OXYGEN)
                + ", spare parts=" + w.colony().getStock().total(ResourceType.SPARE_PARTS)
                + ", battery=" + w.colony().getStock().total(ResourceType.BATTERY_POWER));
    }

    private static void text(Consumer<String> out, String text) {
        for (String line : text.split("\\R")) out.accept(line);
    }

    private static void heading(Consumer<String> out, String text) {
        out.accept("");
        out.accept("==================================================");
        out.accept(text);
        out.accept("==================================================");
    }
}
