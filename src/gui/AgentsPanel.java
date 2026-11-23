package gui;

import agents.Drone;
import agents.Engineer;
import agents.ExplorationRover;
import agents.MedicalOfficer;
import agents.RepairRobot;
import agents.Scientist;
import assets.facilities.Facility;
import enums.Equipment;
import interfaces.IAgent;
import interfaces.ILocation;
import interfaces.IPerson;
import interfaces.IRobotUnit;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
final class AgentsPanel extends JPanel {
    private enum AgentType {
        SCIENTIST("Scientist"), ENGINEER("Engineer"), MEDICAL_OFFICER("Medical officer"),
        EXPLORATION_ROVER("Exploration rover"), REPAIR_ROBOT("Repair robot"), DRONE("Drone");

        private final String label;
        AgentType(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final AppState state;
    private final DefaultTableModel model = Ui.model(
            "ID", "Name", "Kind", "Type", "Location", "Status", "Usable", "Condition", "Equipment");
    private final JTable table = Ui.table(model);
    private final JComboBox<AgentType> typeBox = new JComboBox<>(AgentType.values());
    private final JTextField nameField = new JTextField(14);
    private final JComboBox<Item<ILocation>> locationBox = new JComboBox<>();

    AgentsPanel(AppState state) {
        this.state = state;
        setLayout(new BorderLayout(8, 8));
        setBorder(Ui.padding(8));

        JPanel add = Ui.titled("Add an agent");
        add.add(new JLabel("Type"));
        add.add(typeBox);
        add.add(new JLabel("Name"));
        add.add(nameField);
        add.add(new JLabel("Starts at"));
        add.add(locationBox);
        add.add(Ui.button("Add", this::addAgent));
        nameField.addActionListener(e -> addAgent());
        add(add, BorderLayout.NORTH);

        table.getColumnModel().getColumn(7).setPreferredWidth(200);
        table.getColumnModel().getColumn(8).setPreferredWidth(200);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = Ui.titled("Selected agent");
        actions.setLayout(new GridLayout(0, 4, 6, 6));
        actions.add(Ui.button("Damage robot", this::damage));
        actions.add(Ui.button("Repair robot", this::repair));
        actions.add(Ui.button("Add equipment to rover...", this::addEquipment));
        actions.add(Ui.button("Move to...", this::move));
        actions.add(Ui.button("Change health...", () -> adjust("health")));
        actions.add(Ui.button("Change fatigue...", () -> adjust("fatigue")));
        actions.add(Ui.button("Change battery...", () -> adjust("battery")));
        actions.add(Ui.button("Register in its facility", this::register));
        add(actions, BorderLayout.SOUTH);

        state.addListener(this::refresh);
    }

    private void refresh() {
        Ui.fill(locationBox, Ui.locationItems(state.colony()));
        List<Object[]> rows = new ArrayList<>();
        for (IAgent a : state.colony().getAgents()) {
            rows.add(new Object[]{a.getId(), a.getName(), a.getKind(), a.getClass().getSimpleName(),
                    a.getLocation().getName(), a.isAvailable() ? "available" : "on mission",
                    a.isUsable() ? "yes" : "no", condition(a), equipment(a)});
        }
        Ui.reload(table, rows);
    }

    private static String condition(IAgent a) {
        if (a instanceof IRobotUnit r) return "battery " + r.getBatteryLevel() + (r.isDamaged() ? ", DAMAGED" : "");
        if (a instanceof IPerson p) return "health " + p.getHealth() + ", fatigue " + p.getFatigue();
        return "";
    }

    private static String equipment(IAgent a) {
        if (!(a instanceof ExplorationRover rover)) return "";
        StringBuilder sb = new StringBuilder();
        rover.getEquipmentInventory().forEach((item, count) -> {
            if (count > 0) sb.append(sb.length() == 0 ? "" : ", ").append(item).append(" x").append(count);
        });
        return sb.toString();
    }

    private IAgent selected() {
        Object id = Ui.selectedKey(table);
        if (id == null) throw new IllegalStateException("Select an agent in the table first");
        for (IAgent a : state.colony().getAgents()) if (a.getId().equals(id)) return a;
        throw new IllegalStateException("The selected agent no longer exists");
    }

    private void addAgent() {
        Ui.run(state, () -> {
            AgentType type = (AgentType) typeBox.getSelectedItem();
            ILocation location = Ui.selectedValue(locationBox, "a starting location");
            String name = nameField.getText();
            IAgent agent = switch (type) {
                case SCIENTIST -> new Scientist(name, location);
                case ENGINEER -> new Engineer(name, location);
                case MEDICAL_OFFICER -> new MedicalOfficer(name, location);
                case EXPLORATION_ROVER -> new ExplorationRover(name, location);
                case REPAIR_ROBOT -> new RepairRobot(name, location);
                case DRONE -> new Drone(name, location);
            };
            state.colony().addAgent(agent);
            state.log("Added " + agent.getId() + " " + agent.getName() + " (" + type + ") at " + location.getName());
            nameField.setText("");
        });
    }

    private void damage() {
        Ui.run(state, () -> {
            IRobotUnit robot = robot();
            robot.damage();
            state.log(((IAgent) robot).getName() + " is now damaged");
        });
    }

    private void repair() {
        Ui.run(state, () -> {
            IRobotUnit robot = robot();
            robot.repair();
            state.log(((IAgent) robot).getName() + " was repaired");
        });
    }

    private IRobotUnit robot() {
        if (selected() instanceof IRobotUnit robot) return robot;
        throw new IllegalStateException("The selected agent is not a robot");
    }

    private void addEquipment() {
        Ui.run(state, () -> {
            if (!(selected() instanceof ExplorationRover rover))
                throw new IllegalStateException("Select an exploration rover to give it equipment");
            JComboBox<Equipment> item = new JComboBox<>(Equipment.values());
            JSpinner amount = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            if (!Ui.formHandler.test("Add equipment to " + rover.getName(), Ui.form("Equipment", item, "Amount", amount))) return;
            rover.addEquipment((Equipment) item.getSelectedItem(), (Integer) amount.getValue());
            state.log(rover.getName() + " received " + amount.getValue() + " x " + item.getSelectedItem());
        });
    }

    private void move() {
        Ui.run(state, () -> {
            IAgent agent = selected();
            JComboBox<Item<ILocation>> where = new JComboBox<>();
            Ui.fill(where, Ui.locationItems(state.colony()));
            if (!Ui.formHandler.test("Move " + agent.getName(), Ui.form("New location", where))) return;
            ILocation location = Ui.selectedValue(where, "a location");
            agent.setLocation(location);
            state.log(agent.getName() + " moved to " + location.getName());
        });
    }

    private void adjust(String what) {
        Ui.run(state, () -> {
            IAgent agent = selected();
            JSpinner delta = new JSpinner(new SpinnerNumberModel(-10, -100, 100, 5));
            boolean person = agent instanceof IPerson;
            if (what.equals("battery") == person)
                throw new IllegalStateException(what.equals("battery") ? "Battery applies to robots only"
                        : "Health and fatigue apply to people only");
            if (!Ui.formHandler.test("Change " + what + " of " + agent.getName(),
                    Ui.form("Change by (can be negative)", delta))) return;
            int amount = (Integer) delta.getValue();
            switch (what) {
                case "health" -> ((IPerson) agent).changeHealthBy(amount);
                case "fatigue" -> ((IPerson) agent).changeFatigueBy(amount);
                default -> ((IRobotUnit) agent).changeBatteryLevel(amount);
            }
            state.log(agent.getName() + ": " + what + " changed by " + amount);
        });
    }

    private void register() {
        Ui.run(state, () -> {
            IAgent agent = selected();
            if (!(agent instanceof IPerson person) || !(agent.getLocation() instanceof Facility facility))
                throw new IllegalStateException("Select a person who is located in a facility");
            facility.addPerson(person);
            state.log(agent.getName() + " registered in " + facility.getName());
        });
    }
}
