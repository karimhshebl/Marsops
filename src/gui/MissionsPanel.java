package gui;

import Missions.ExplorationMission;
import Missions.Mission;
import Missions.RepairMission;
import Missions.RescueMission;
import Missions.ResearchMission;
import assets.facilities.Facility;
import enums.MissionType;
import interfaces.IAgent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
final class MissionsPanel extends JPanel {
    private final AppState state;
    private final JComboBox<MissionType> typeBox = new JComboBox<>(MissionType.values());
    private final JSpinner duration = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
    private final JList<Item<Facility>> targets = new JList<>();
    private final JList<Item<IAgent>> agents = new JList<>();
    private final JLabel requirements = new JLabel();
    private final JLabel result = new JLabel("No mission submitted yet");
    private final JLabel active = new JLabel();
    private final DefaultTableModel model = Ui.model("ID", "Type", "State", "Progress", "Priority", "Agents", "Failure reason");
    private final JTable table = Ui.table(model);

    MissionsPanel(AppState state) {
        this.state = state;
        setLayout(new BorderLayout(8, 8));
        setBorder(Ui.padding(8));

        targets.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        agents.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel basics = Ui.titled("New mission");
        basics.add(new JLabel("Type"));
        basics.add(typeBox);
        basics.add(new JLabel("Duration (time units)"));
        basics.add(duration);
        basics.add(Ui.button("Submit mission", this::submit));
        basics.add(Ui.button("Advance 1 time unit", () -> Ui.run(state, () -> state.simulation().advanceTime(1))));
        top.add(basics, BorderLayout.NORTH);

        JPanel pickers = new JPanel(new GridLayout(1, 2, 8, 8));
        pickers.add(scroll(targets, "Target facilities (optional, hold Ctrl to select several)"));
        pickers.add(scroll(agents, "Agents for the mission (hold Ctrl to select several)"));
        pickers.setPreferredSize(new Dimension(100, 150));
        top.add(pickers, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.add(requirements);
        info.add(result);
        info.add(active);
        top.add(info, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        typeBox.addActionListener(e -> showRequirements());
        showRequirements();
        state.addListener(this::refresh);
    }

    private static JScrollPane scroll(JList<?> list, String title) {
        JScrollPane pane = new JScrollPane(list);
        pane.setBorder(javax.swing.BorderFactory.createTitledBorder(title));
        return pane;
    }

    private void showRequirements() {
        MissionType type = (MissionType) typeBox.getSelectedItem();
        requirements.setText("<html><b>Needs:</b> " + switch (type) {
            case EXPLORATION -> "FUEL 10, BATTERY_POWER 15. A scientist, plus a rover carrying a SOIL_ANALYZER. Not allowed in a dust storm or communication disruption.";
            case REPAIR -> "SPARE_PARTS 2, BATTERY_POWER 5. An engineer and a repair robot. Choose a damaged facility as the target.";
            case RESEARCH -> "OXYGEN 10, BATTERY_POWER 20. A scientist. Choose an operational laboratory with LABORATORY_APPARATUS as the target.";
            case RESCUE -> "FUEL 15, OXYGEN 10, MEDICAL_SUPPLY 2. A rover, a person, and an agent with communication. Not allowed in a dust storm or communication disruption.";
        } + "</html>");
    }

    private void refresh() {
        List<Item<Facility>> facilityItems = new ArrayList<>();
        for (Facility f : state.colony().getFacilities()) facilityItems.add(new Item<>(Ui.facilityLabel(f), f));
        Ui.fill(targets, facilityItems);

        List<Item<IAgent>> agentItems = new ArrayList<>();
        for (IAgent a : state.colony().getAgents()) {
            agentItems.add(new Item<>(Ui.agentLabel(a) + " - " + a.getLocation().getName()
                    + (a.isAvailable() ? "" : " - ON MISSION"), a));
        }
        Ui.fill(agents, agentItems);

        Mission current = state.colony().getActiveMission();
        active.setText("Active mission: " + (current == null ? "none" : current));

        List<Object[]> rows = new ArrayList<>();
        if (current != null) rows.add(row(current));
        List<Mission> history = state.colony().getMissionHistory();
        for (int i = history.size() - 1; i >= 0; i--) rows.add(row(history.get(i)));
        Ui.reload(table, rows);
    }

    private static Object[] row(Mission m) {
        StringBuilder names = new StringBuilder();
        for (IAgent a : m.getAgents()) names.append(names.length() == 0 ? "" : ", ").append(a.getName());
        return new Object[]{m.getId(), m.getType(), m.getState(), m.getElapsed() + " / " + m.getDuration(),
                m.getPriority(), names.toString(), m.getFailureReason() == null ? "" : m.getFailureReason()};
    }

    private void submit() {
        Ui.run(state, () -> {
            MissionType type = (MissionType) typeBox.getSelectedItem();
            int length = (Integer) duration.getValue();
            List<Facility> chosenTargets = Ui.values(targets);
            List<IAgent> chosenAgents = Ui.values(agents);
            Mission mission = switch (type) {
                case EXPLORATION -> new ExplorationMission(length, chosenTargets);
                case REPAIR -> new RepairMission(length, chosenTargets);
                case RESEARCH -> new ResearchMission(length, chosenTargets);
                case RESCUE -> new RescueMission(length, chosenTargets);
            };
            if (state.colony().submitMission(mission, chosenAgents)) {
                String text = "Mission " + mission.getId() + " (" + type + ") accepted: " + mission.getState();
                result.setText(text);
                state.log(text);
            } else {
                String text = type + " mission cannot be assigned. Reason: " + mission.getFailureReason();
                result.setText(text);
                state.log(mission.getId() + ": " + text);
            }
        });
    }
}
