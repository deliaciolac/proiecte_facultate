package gui.manager;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class StatsPanel extends JPanel {

    private final ClientConnection conn;
    private final String token;

    private final JTextArea area = new JTextArea();

    public StatsPanel(ClientConnection conn, String token) {
        this.conn = conn;
        this.token = token;

        setLayout(new BorderLayout(8, 8));

        JButton refresh = new JButton("Refresh statistici");
        add(refresh, BorderLayout.NORTH);

        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        refresh.addActionListener(e -> loadStats());

        loadStats();
    }

    private void loadStats() {
        try {
            Request req = new Request(Action.MANAGER_GET_STATS);
            req.setToken(token);

            Response r = conn.send(req);
            if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) r.getData().get("stats");

            area.setText("");
            if (stats == null || stats.isEmpty()) {
                area.setText("Nicio statistica disponibila.\n");
                return;
            }

            for (var e : stats.entrySet()) {
                area.append(e.getKey() + " = " + e.getValue() + "\n");
            }

        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }
}