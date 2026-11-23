package gui;

import scenario.Scenarios;
import scenario.Scenarios.Scenario;
import scenario.Scenarios.World;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;

@SuppressWarnings("serial")
final class ScenariosPanel extends JPanel {
    private final AppState state;
    private final Runnable showLog;
    private final JList<Scenario> list;
    private final JTextArea description = new JTextArea();

    ScenariosPanel(AppState state, Runnable showLog) {
        this.state = state;
        this.showLog = showLog;
        setLayout(new BorderLayout(8, 8));
        setBorder(Ui.padding(8));

        DefaultListModel<Scenario> model = new DefaultListModel<>();
        Scenarios.all().forEach(model::addElement);
        list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(e -> showDescription());
        JScrollPane listPane = new JScrollPane(list);
        listPane.setPreferredSize(new Dimension(360, 100));
        add(listPane, BorderLayout.WEST);

        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBorder(Ui.padding(8));
        add(new JScrollPane(description), BorderLayout.CENTER);

        JPanel south = Ui.titled("Run");
        south.add(Ui.button("Run selected scenario", this::runSelected));
        south.add(Ui.button("Run all scenarios", this::runAll));
        south.add(new JLabel("Each scenario builds a new colony, replaces the current one, and writes what happens to the event log."));
        add(south, BorderLayout.SOUTH);

        showDescription();
    }

    private void showDescription() {
        Scenario s = list.getSelectedValue();
        description.setText(s == null ? "" : s.title() + "\n\n" + s.description()
                + "\n\nAfter it runs, open the other tabs to inspect the colony it ended with.");
        description.setCaretPosition(0);
    }

    private void runSelected() {
        Ui.run(state, () -> {
            Scenario s = list.getSelectedValue();
            if (s == null) throw new IllegalStateException("Select a scenario first");
            load(s.run(state.logger()));
        });
        showLog.run();
    }

    private void runAll() {
        Ui.run(state, () -> {
            World last = null;
            for (Scenario s : Scenarios.all()) last = s.run(state.logger());
            if (last != null) load(last);
        });
        showLog.run();
    }

    private void load(World world) {
        state.load(world.colony(), world.simulation());
    }
}
