package gui.mechanic;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;
import model.Appointment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MechanicPanel extends JPanel {

    private final ClientConnection conn;
    private final String token;

    private final DefaultTableModel model;
    private final JTable table;

    public MechanicPanel(ClientConnection conn, String token) {
        this.conn = conn;
        this.token = token;

        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        JButton status = new JButton("Update status (selecteaza)");
        JButton details = new JButton("Adauga piese + durata + note (selecteaza)");

        top.add(refresh);
        top.add(status);
        top.add(details);

        model = new DefaultTableModel(new Object[]{
                "ID", "Tip", "Data/Ora", "Status", "Vehicul", "Client notes"
        }, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh.addActionListener(e -> refreshAssigned());

        status.addActionListener(e -> {
            Long id = selectedId();
            if (id == null) { Dialogs.error("Selecteaza o lucrare."); return; }
            StatusDialog d = new StatusDialog(conn, token, id, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            refreshAssigned();
        });

        details.addActionListener(e -> {
            Long id = selectedId();
            if (id == null) { Dialogs.error("Selecteaza o lucrare."); return; }
            PartsDurationDialog d = new PartsDurationDialog(conn, token, id, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            refreshAssigned();
        });

        refreshAssigned();
    }

    private Long selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        if (v instanceof Long) return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();
        return Long.parseLong(v.toString());
    }

    private void refreshAssigned() {
        try {
            Request req = new Request(Action.MECHANIC_LIST_ASSIGNED);
            req.setToken(token);
            Response r = conn.send(req);
            if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

            @SuppressWarnings("unchecked")
            List<Appointment> list = (List<Appointment>) r.getData().get("appointments");

            model.setRowCount(0);
            if (list != null) {
                for (Appointment a : list) {
                    model.addRow(new Object[]{
                            a.getId(),
                            a.getServiceType(),
                            String.valueOf(a.getScheduledAt()),
                            a.getStatus(),
                            a.getVehicleInfo(),
                            a.getClientNotes()
                    });
                }
            }
        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }
}