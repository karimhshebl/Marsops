package gui;

import agents.ExplorationRover;
import assets.facilities.Facility;
import assets.facilities.Habitat;
import assets.facilities.Laboratory;
import emergency.Emergency;
import emergency.FireEmergency;
import emergency.MedicalEmergency;
import emergency.OxygenLeakEmergency;
import emergency.RoverBreakdownEmergency;
import enums.EmergencyType;
import interfaces.IAgent;
import interfaces.IPerson;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
final class EmergenciesPanel extends JPanel {
    private final AppState state;
    private final JComboBox<EmergencyType> typeBox = new JComboBox<>(EmergencyType.values());
    private final JComboBox<Item<Object>> targetBox = new JComboBox<>();
    private final JSpinner oxygenLoss = new JSpinner(new SpinnerNumberModel(30, 1, 10000, 5));
    private final JSpinner scheduleTime = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    private final JLabel targetLabel = new JLabel();
    private final JLabel requirements = new JLabel();
    private final JLabel result = new JLabel("No emergency triggered yet");
    private final DefaultTableModel model = Ui.model("ID", "Type", "Status", "Location", "Reason");
    private final JTable table = Ui.table(model);

    EmergenciesPanel(AppState state) {
        this.state = state;
        setLayout(new BorderLayout(8, 8));
        setBorder(Ui.padding(8));

        JPanel form = Ui.titled("Trigger an emergency");
        form.add(new JLabel("Type"));
        form.add(typeBox);
        form.add(targetLabel);
        form.add(targetBox);
        form.add(new JLabel("Oxygen lost (leak only)"));
        form.add(oxygenLoss);
        form.add(Ui.button("Trigger now", () -> trigger(false)));
        form.add(new JLabel("or at time"));
        form.add(scheduleTime);
        form.add(Ui.button("Schedule", () -> trigger(true)));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.add(requirements);
        info.add(result);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.NORTH);
        top.add(info, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        typeBox.addActionListener(e -> { showType(); refreshTargets(); });
        showType();
        state.addListener(this::refresh);
    }

    private void showType() {
        EmergencyType type = (EmergencyType) typeBox.getSelectedItem();
        targetLabel.setText(switch (type) {
            case OXYGEN_LEAK -> "Habitat";
            case FIRE -> "Laboratory";
            case MEDICAL -> "Patient";
            case ROVER_BREAKDOWN -> "Rover";
        });
        oxygenLoss.setEnabled(type == EmergencyType.OXYGEN_LEAK);
        requirements.setText("<html><b>Responders:</b> " + switch (type) {
            case OXYGEN_LEAK -> "1 human and 1 robot that can repair. Cost: SPARE_PARTS 2, BATTERY_POWER 5.";
            case FIRE -> "1 human that can fight fires (a medical officer). Cost: WATER 10, BATTERY_POWER 5.";
            case MEDICAL -> "1 human with medical care. Cost: MEDICAL_SUPPLY 2.";
            case ROVER_BREAKDOWN -> "1 human and 1 robot that can repair. Cost: SPARE_PARTS 2, BATTERY_POWER 5.";
        } + " Responders must be available (not on a mission) and usable.</html>");
    }

    private void refresh() {
        scheduleTime.setValue(Math.max((Integer) scheduleTime.getValue(), state.simulation().getCurrentTime() + 1));
        refreshTargets();
        List<Object[]> rows = new ArrayList<>();
        List<Emergency> history = state.colony().getEmergencyHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            Emergency e = history.get(i);
            rows.add(new Object[]{e.getId(), e.getType(), e.getStatus(), e.getLocation().getName(),
                    e.getReason() == null ? "" : e.getReason()});
        }
        Ui.reload(table, rows);
    }

    private void refreshTargets() {
        EmergencyType type = (EmergencyType) typeBox.getSelectedItem();
        List<Item<Object>> items = new ArrayList<>();
        switch (type) {
            case OXYGEN_LEAK -> {
                for (Facility f : state.colony().getFacilities())
                    if (f instanceof Habitat) items.add(new Item<>(Ui.facilityLabel(f), f));
            }
            case FIRE -> {
                for (Facility f : state.colony().getFacilities())
                    if (f instanceof Laboratory) items.add(new Item<>(Ui.facilityLabel(f), f));
            }
            case MEDICAL -> {
                for (IAgent a : state.colony().getAgents())
                    if (a instanceof IPerson) items.add(new Item<>(Ui.agentLabel(a), a));
            }
            case ROVER_BREAKDOWN -> {
                for (IAgent a : state.colony().getAgents())
                    if (a instanceof ExplorationRover) items.add(new Item<>(Ui.agentLabel(a), a));
            }
        }
        Ui.fill(targetBox, items);
    }

    private Emergency create(EmergencyType type) {
        Object target = Ui.selectedValue(targetBox, targetLabel.getText().toLowerCase());
        return switch (type) {
            case OXYGEN_LEAK -> new OxygenLeakEmergency((Habitat) target, (Integer) oxygenLoss.getValue());
            case FIRE -> new FireEmergency((Laboratory) target, state.colony().getResourceInventory());
            case MEDICAL -> new MedicalEmergency((IPerson) target);
            case ROVER_BREAKDOWN -> new RoverBreakdownEmergency((ExplorationRover) target);
        };
    }

    private void trigger(boolean schedule) {
        Ui.run(state, () -> {
            Emergency emergency = create((EmergencyType) typeBox.getSelectedItem());
            if (schedule) {
                int time = (Integer) scheduleTime.getValue();
                state.simulation().scheduleEmergency(time, emergency);
                String text = emergency.getId() + " (" + emergency.getType() + ") scheduled for time " + time;
                result.setText(text);
                state.log(text);
            } else {
                state.colony().triggerEmergency(emergency);
                result.setText(emergency.toString());
                state.log("Emergency: " + emergency);
            }
        });
    }
}
