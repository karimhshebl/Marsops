package gui;

import assets.resources.FirstComeFirstServedPolicy;
import assets.resources.TieredAllocationPolicy;
import colony.MarsColony;
import interfaces.IAllocationPolicy;
import scenario.Scenarios;
import universalHelpers.IdGenerator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Font;

@SuppressWarnings("serial")
public class MarsOpsApp extends JFrame {
    private final AppState state = new AppState();
    private final JTextArea log = new JTextArea();
    private final JSplitPane split;
    private final JTabbedPane tabs = new JTabbedPane();

    public MarsOpsApp() {
        super("MARS-OPS: Mars Colony Management and Emergency Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Ui.parent = this;
        setSize(1280, 860);
        setLocationRelativeTo(null);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        state.setSink(line -> {
            log.append(line + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });

        tabs.addTab("Overview", new OverviewPanel(state));
        tabs.addTab("Agents", new AgentsPanel(state));
        tabs.addTab("Facilities", new FacilitiesPanel(state));
        tabs.addTab("Resources", new ResourcesPanel(state));
        tabs.addTab("Missions", new MissionsPanel(state));
        tabs.addTab("Emergencies", new EmergenciesPanel(state));
        tabs.addTab("Scenarios", new ScenariosPanel(state, this::showLog));

        JPanel logPanel = new JPanel(new BorderLayout());
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBorder(Ui.padding(4));
        logHeader.add(new JLabel("Event log"), BorderLayout.WEST);
        logHeader.add(Ui.button("Clear", () -> log.setText("")), BorderLayout.EAST);
        logPanel.add(logHeader, BorderLayout.NORTH);
        logPanel.add(new JScrollPane(log), BorderLayout.CENTER);

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, logPanel);
        split.setResizeWeight(0.72);
        split.setContinuousLayout(true);

        add(new ControlBar(state), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        setJMenuBar(menu());

        Scenarios.World demo = Scenarios.buildWorld(50, state.logger());
        state.load(demo.colony(), demo.simulation());
        state.log("Demo colony ready: 3 facilities with stock, no agents yet. Add agents in the Agents tab or run a scenario.");
    }

    private JMenuBar menu() {
        JMenu colonyMenu = new JMenu("Colony");
        colonyMenu.add(item("New demo colony (facilities and stock)", () -> {
            Scenarios.World w = Scenarios.buildWorld(50, state.logger());
            state.load(w.colony(), w.simulation());
            state.log("New demo colony created");
        }));
        colonyMenu.add(item("New empty colony (tiered allocation policy)", () -> empty(new TieredAllocationPolicy(), "tiered")));
        colonyMenu.add(item("New empty colony (first come first served)", () -> empty(new FirstComeFirstServedPolicy(), "first come first served")));
        colonyMenu.addSeparator();
        colonyMenu.add(item("Exit", () -> System.exit(0)));
        JMenuBar bar = new JMenuBar();
        bar.add(colonyMenu);
        return bar;
    }

    private void empty(IAllocationPolicy policy, String name) {
        IdGenerator.reset();
        state.newColony(new MarsColony("Ares Base", policy));
        state.log("New empty colony created (" + name + " allocation policy). Add facilities first.");
    }

    private static JMenuItem item(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> action.run());
        return item;
    }

    private void showLog() {
        split.setDividerLocation(0.4);
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new MarsOpsApp().setVisible(true));
    }

    public static void main(String[] args) {
        launch();
    }
}
