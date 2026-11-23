package gui;

import enums.ResourceType;
import interfaces.IStockView;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
final class ResourcesPanel extends JPanel {
    private final AppState state;
    private final DefaultTableModel model = Ui.model("Resource", "Total", "Reserved", "Free", "Use per time unit", "Time units left");
    private final JTable table = Ui.table(model);
    private final JComboBox<ResourceType> typeBox = new JComboBox<>(ResourceType.values());
    private final JSpinner rate = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));

    ResourcesPanel(AppState state) {
        this.state = state;
        setLayout(new BorderLayout(8, 8));
        setBorder(Ui.padding(8));

        JLabel note = new JLabel("<html>Colony-wide totals count only operational facilities. "
                + "Destroyed and unavailable facilities are not counted. Add stock to a facility in the Facilities tab.</html>");
        add(note, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel consumption = Ui.titled("Colony consumption");
        consumption.add(new JLabel("Resource"));
        consumption.add(typeBox);
        consumption.add(new JLabel("Used each time unit"));
        consumption.add(rate);
        consumption.add(Ui.button("Set", this::setRate));
        add(consumption, BorderLayout.SOUTH);

        state.addListener(this::refresh);
    }

    private void refresh() {
        IStockView stock = state.colony().getStock();
        List<Object[]> rows = new ArrayList<>();
        for (ResourceType type : ResourceType.values()) {
            int total = stock.total(type);
            int free = stock.free(type);
            int use = stock.consumptionPerTick(type);
            rows.add(new Object[]{type, total, total - free, free, use, use > 0 ? String.valueOf(total / use) : "-"});
        }
        Ui.reload(table, rows);
    }

    private void setRate() {
        Ui.run(state, () -> {
            ResourceType type = (ResourceType) typeBox.getSelectedItem();
            int value = (Integer) rate.getValue();
            state.colony().getResourceInventory().setConsumptionPerTick(type, value);
            state.log("Colony now uses " + value + " " + type + " per time unit");
        });
    }
}
