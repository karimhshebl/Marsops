package gui;

import assets.facilities.Facility;
import colony.MarsColony;
import interfaces.IAgent;
import interfaces.ILocation;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

final class Ui {
    private Ui() { }

    static java.awt.Component parent;

    static Consumer<String> errorHandler = message ->
            JOptionPane.showMessageDialog(parent, message, "Cannot do that", JOptionPane.ERROR_MESSAGE);
    static BiPredicate<String, JComponent> formHandler = (title, content) ->
            JOptionPane.showConfirmDialog(parent, content, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    static BiPredicate<String, String> confirmHandler = (title, message) ->
            JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;

    static void run(AppState state, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            state.log("Not done: " + message);
            errorHandler.accept(message);
        }
        state.refresh();
    }

    static JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    static JPanel titled(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    static Border padding(int n) { return BorderFactory.createEmptyBorder(n, n, n, n); }

    static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(22);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    static void reload(JTable table, List<Object[]> rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object key = selectedKey(table);
        model.setRowCount(0);
        for (Object[] row : rows) model.addRow(row);
        if (key == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (key.equals(model.getValueAt(i, 0))) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    static Object selectedKey(JTable table) {
        int row = table.getSelectedRow();
        return row < 0 ? null : table.getModel().getValueAt(row, 0);
    }

    static <T> void fill(JComboBox<Item<T>> combo, List<Item<T>> items) {
        @SuppressWarnings("unchecked")
        Item<T> selected = (Item<T>) combo.getSelectedItem();
        DefaultComboBoxModel<Item<T>> model = new DefaultComboBoxModel<>();
        for (Item<T> item : items) model.addElement(item);
        combo.setModel(model);
        if (selected != null && items.contains(selected)) combo.setSelectedItem(selected);
    }

    static <T> void fill(JList<Item<T>> list, List<Item<T>> items) {
        List<Item<T>> selected = list.getSelectedValuesList();
        DefaultListModel<Item<T>> model = new DefaultListModel<>();
        for (Item<T> item : items) model.addElement(item);
        list.setModel(model);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) if (selected.contains(items.get(i))) indices.add(i);
        list.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
    }

    @SuppressWarnings("unchecked")
    static <T> T selectedValue(JComboBox<Item<T>> combo, String what) {
        Item<T> item = (Item<T>) combo.getSelectedItem();
        if (item == null) throw new IllegalStateException("Select " + what + " first");
        return item.value();
    }

    static <T> List<T> values(JList<Item<T>> list) {
        List<T> result = new ArrayList<>();
        for (Item<T> item : list.getSelectedValuesList()) result.add(item.value());
        return result;
    }

    static JPanel form(Object... labelsAndFields) {
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        for (int i = 0; i < labelsAndFields.length; i += 2) {
            p.add(new JLabel((String) labelsAndFields[i]));
            p.add((Component) labelsAndFields[i + 1]);
        }
        return p;
    }

    static List<Item<ILocation>> locationItems(MarsColony colony) {
        List<Item<ILocation>> items = new ArrayList<>();
        for (Facility f : colony.getFacilities()) items.add(new Item<>(facilityLabel(f), f));
        items.add(new Item<>(colony.getSurface().getName(), colony.getSurface()));
        return items;
    }

    static String facilityLabel(Facility f) {
        return f.getId() + "  " + f.getName() + "  (" + f.getClass().getSimpleName() + ")";
    }

    static String agentLabel(IAgent a) {
        return a.getId() + "  " + a.getName() + "  (" + a.getClass().getSimpleName() + ")";
    }

    static String statusOf(Facility f) {
        if (f.isDestroyed()) return "DESTROYED";
        if (f.isUnavailable()) return "UNAVAILABLE";
        if (f.isDamaged()) return "DAMAGED";
        return "OPERATIONAL";
    }
}
