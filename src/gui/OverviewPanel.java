package gui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;

@SuppressWarnings("serial")
final class OverviewPanel extends JPanel {
    private final AppState state;
    private final JTextArea text = new JTextArea();

    OverviewPanel(AppState state) {
        this.state = state;
        setLayout(new BorderLayout(8, 8));
        setBorder(Ui.padding(8));
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(text), BorderLayout.CENTER);
        state.addListener(this::refresh);
    }

    private void refresh() {
        text.setText(state.simulation().getStatusReport());
        text.setCaretPosition(0);
    }
}
