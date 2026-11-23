package gui;

import enums.EnvironmentCondition;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

@SuppressWarnings("serial")
final class ControlBar extends JPanel {
    private final AppState state;
    private final JLabel colonyLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JLabel environmentLabel = new JLabel();
    private final JComboBox<EnvironmentCondition> environmentBox = new JComboBox<>(EnvironmentCondition.values());
    private final JSpinner units = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    ControlBar(AppState state) {
        this.state = state;
        setLayout(new java.awt.GridLayout(2, 1));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8));

        Font bold = colonyLabel.getFont().deriveFont(Font.BOLD, 14f);
        colonyLabel.setFont(bold);
        timeLabel.setFont(bold);
        environmentLabel.setFont(bold);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 2));
        info.add(colonyLabel);
        info.add(timeLabel);
        info.add(environmentLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        controls.add(new JLabel("Advance time by"));
        controls.add(units);
        controls.add(Ui.button("Advance", () -> Ui.run(state, () -> state.simulation().advanceTime((Integer) units.getValue()))));
        controls.add(new JLabel("      Environment"));
        controls.add(environmentBox);
        controls.add(Ui.button("Set now", () -> Ui.run(state, () -> {
            EnvironmentCondition env = (EnvironmentCondition) environmentBox.getSelectedItem();
            state.colony().setEnvironment(env);
            state.log("Environment set to " + env);
        })));
        controls.add(Ui.button("Schedule change...", this::schedule));

        add(info);
        add(controls);
        state.addListener(this::refresh);
    }

    private void refresh() {
        colonyLabel.setText("Colony: " + state.colony().getName());
        timeLabel.setText("Time: " + state.simulation().getCurrentTime());
        EnvironmentCondition env = state.colony().getEnvironment();
        environmentLabel.setText("Environment: " + env);
        environmentLabel.setForeground(switch (env) {
            case NORMAL -> new Color(0x1B7F3B);
            case DUST_STORM -> new Color(0xB35C00);
            case COMMUNICATION_DISRUPTION -> new Color(0xB00020);
        });
        environmentBox.setSelectedItem(env);
    }

    private void schedule() {
        Ui.run(state, () -> {
            JSpinner time = new JSpinner(new SpinnerNumberModel(state.simulation().getCurrentTime() + 1, 1, 100000, 1));
            JComboBox<EnvironmentCondition> env = new JComboBox<>(EnvironmentCondition.values());
            if (!Ui.formHandler.test("Schedule an environment change", Ui.form("At time", time, "Change to", env))) return;
            state.simulation().scheduleEnvironmentChange((Integer) time.getValue(), (EnvironmentCondition) env.getSelectedItem());
            state.log("Environment change to " + env.getSelectedItem() + " scheduled for time " + time.getValue());
        });
    }
}
