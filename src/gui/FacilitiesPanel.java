package gui;

import assets.facilities.CommunicationStation;
import assets.facilities.Facility;
import assets.facilities.Habitat;
import assets.facilities.Laboratory;
import assets.facilities.PowerStation;
import assets.facilities.StorageFacility;
import enums.Equipment;
import enums.ResourceType;

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
final class FacilitiesPanel extends JPanel {
    private enum FacilityType {
        HABITAT("Habitat"), LABORATORY("Laboratory"), COMMUNICATION_STATION("Communication station"),
        POWER_STATION("Power station"), STORAGE_FACILITY("Storage facility");

        private final String label;
        FacilityType(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final AppState state;
    private final DefaultTableModel model = Ui.model(
            "ID", "Name", "Type", "Status", "Capacity", "People", "Stock", "Equipment");
    private final JTable table = Ui.table(model);
    private final JComboBox<FacilityType> typeBox = new JComboBox<>(FacilityType.values());
    private final JTextField nameField = new JTextField(14);

    FacilitiesPanel(AppState state) {
        this.state = state;
        setLayout(new BorderLayout(8, 8));
        setBorder(Ui.padding(8));

        JPanel add = Ui.titled("Add a facility");
        add.add(new JLabel("Type"));
        add.add(typeBox);
        add.add(new JLabel("Name"));
        add.add(nameField);
        add.add(Ui.button("Add", this::addFacility));
        nameField.addActionListener(e -> addFacility());
        add(add, BorderLayout.NORTH);

        table.getColumnModel().getColumn(6).setPreferredWidth(420);
        table.getColumnModel().getColumn(7).setPreferredWidth(200);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new GridLayout(2, 1, 6, 6));
        JPanel status = Ui.titled("Selected facility: status");
        status.setLayout(new GridLayout(0, 5, 6, 6));
        status.add(Ui.button("Damage", () -> act("damaged", Facility::damage)));
        status.add(Ui.button("Repair", () -> act("repaired", Facility::repair)));
        status.add(Ui.button("Make unavailable", () -> act("made unavailable", Facility::makeUnavailable)));
        status.add(Ui.button("Restore", () -> act("restored", Facility::restore)));
        status.add(Ui.button("Destroy", this::destroy));
        JPanel contents = Ui.titled("Selected facility: contents");
        contents.setLayout(new GridLayout(0, 4, 6, 6));
        contents.add(Ui.button("Add resources...", this::addResource));
        contents.add(Ui.button("Add lab equipment...", this::addEquipment));
        contents.add(Ui.button("Damage lab equipment", () -> lab(true)));
        contents.add(Ui.button("Repair lab equipment", () -> lab(false)));
        south.add(status);
        south.add(contents);
        add(south, BorderLayout.SOUTH);

        state.addListener(this::refresh);
    }

    private void refresh() {
        List<Object[]> rows = new ArrayList<>();
        for (Facility f : state.colony().getFacilities()) {
            rows.add(new Object[]{f.getId(), f.getName(), f.getClass().getSimpleName(), Ui.statusOf(f),
                    f.getCapacity(), f.getPersonCount(), stock(f), equipment(f)});
        }
        Ui.reload(table, rows);
    }

    private static String stock(Facility f) {
        StringBuilder sb = new StringBuilder();
        for (ResourceType type : ResourceType.values()) {
            int quantity = f.getInventory().getQuantity(type);
            if (quantity > 0) sb.append(sb.length() == 0 ? "" : ", ").append(type).append('=').append(quantity);
        }
        return sb.toString();
    }

    private static String equipment(Facility f) {
        if (!(f instanceof Laboratory lab)) return "";
        if (lab.isEquipmentDamaged()) return "DAMAGED";
        StringBuilder sb = new StringBuilder();
        for (Equipment item : Equipment.values()) {
            int count = lab.getEquipmentCount(item);
            if (count > 0) sb.append(sb.length() == 0 ? "" : ", ").append(item).append(" x").append(count);
        }
        return sb.toString();
    }

    private Facility selected() {
        Object id = Ui.selectedKey(table);
        if (id == null) throw new IllegalStateException("Select a facility in the table first");
        for (Facility f : state.colony().getFacilities()) if (f.getId().equals(id)) return f;
        throw new IllegalStateException("The selected facility no longer exists");
    }

    private void addFacility() {
        Ui.run(state, () -> {
            FacilityType type = (FacilityType) typeBox.getSelectedItem();
            String name = nameField.getText();
            var inventory = state.colony().getResourceInventory();
            Facility facility = switch (type) {
                case HABITAT -> new Habitat(name, inventory);
                case LABORATORY -> new Laboratory(name, inventory);
                case COMMUNICATION_STATION -> new CommunicationStation(name, inventory);
                case POWER_STATION -> new PowerStation(name, inventory);
                case STORAGE_FACILITY -> new StorageFacility(name, inventory);
            };
            state.colony().addFacility(facility);
            state.log("Added " + facility.getId() + " " + facility.getName() + " (" + type + ")");
            nameField.setText("");
        });
    }

    private void act(String done, java.util.function.Consumer<Facility> action) {
        Ui.run(state, () -> {
            Facility f = selected();
            action.accept(f);
            state.log(f.getName() + " " + done);
        });
    }

    private void destroy() {
        Ui.run(state, () -> {
            Facility f = selected();
            if (!Ui.confirmHandler.test("Destroy facility",
                    f.getName() + " will be destroyed permanently and its stock is lost. Continue?")) return;
            f.destroy();
            state.log(f.getName() + " destroyed");
        });
    }

    private void addResource() {
        Ui.run(state, () -> {
            Facility f = selected();
            JComboBox<ResourceType> type = new JComboBox<>(ResourceType.values());
            JSpinner amount = new JSpinner(new SpinnerNumberModel(10, 1, 100000, 5));
            if (!Ui.formHandler.test("Add resources to " + f.getName(), Ui.form("Resource", type, "Amount", amount))) return;
            f.getInventory().addResource((ResourceType) type.getSelectedItem(), (Integer) amount.getValue());
            state.log(f.getName() + " received " + amount.getValue() + " " + type.getSelectedItem());
        });
    }

    private void addEquipment() {
        Ui.run(state, () -> {
            Laboratory lab = laboratory();
            JComboBox<Equipment> item = new JComboBox<>(Equipment.values());
            JSpinner amount = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            if (!Ui.formHandler.test("Add equipment to " + lab.getName(), Ui.form("Equipment", item, "Amount", amount))) return;
            lab.addEquipment((Equipment) item.getSelectedItem(), (Integer) amount.getValue());
            state.log(lab.getName() + " received " + amount.getValue() + " x " + item.getSelectedItem());
        });
    }

    private void lab(boolean damage) {
        Ui.run(state, () -> {
            Laboratory lab = laboratory();
            if (damage) lab.damageEquipment(); else lab.repairEquipment();
            state.log(lab.getName() + " equipment " + (damage ? "damaged" : "repaired"));
        });
    }

    private Laboratory laboratory() {
        if (selected() instanceof Laboratory lab) return lab;
        throw new IllegalStateException("Select a laboratory");
    }
}
