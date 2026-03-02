package gui.client;

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

public class ClientPanel extends JPanel {

    private final ClientConnection conn;
    private final String token;

    private final DefaultTableModel model;
    private final JTable table;

    public ClientPanel(ClientConnection conn, String token) {
        this.conn = conn;
        this.token = token;

        setLayout(new BorderLayout(10, 10));

        JTabbedPane tabs = new JTabbedPane();

        //PROGRAMARI
        JPanel appointmentsTab = new JPanel(new BorderLayout(8, 8));
        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnCreate = new JButton("Creeaza programare");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnNotes = new JButton("Adauga observatii (selecteaza programare)");
        topBtns.add(btnCreate);
        topBtns.add(btnRefresh);
        topBtns.add(btnNotes);

        model = new DefaultTableModel(new Object[]{
                "ID", "Tip", "Data/Ora", "Cost", "Status", "Vehicul", "Observatii client"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        appointmentsTab.add(topBtns, BorderLayout.NORTH);
        appointmentsTab.add(new JScrollPane(table), BorderLayout.CENTER);

        btnCreate.addActionListener(e -> {
            AppointmentCreateDialog d =
                    new AppointmentCreateDialog(conn, token, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            refreshAppointments();
        });

        btnRefresh.addActionListener(e -> refreshAppointments());

        btnNotes.addActionListener(e -> {
            Long id = getSelectedAppointmentId();
            if (id == null) {
                Dialogs.error("Selecteaza o programare din tabel.");
                return;
            }
            NotesDialog d =
                    new NotesDialog(conn, token, id, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            refreshAppointments();
        });

        //NOTIFICARI
        JPanel notificationsTab = new JPanel(new BorderLayout(8, 8));
        JTextArea notificationsArea = new JTextArea();
        notificationsArea.setEditable(false);

        JButton btnGetNotif = new JButton("Verifica notificari");
        notificationsTab.add(btnGetNotif, BorderLayout.NORTH);
        notificationsTab.add(new JScrollPane(notificationsArea), BorderLayout.CENTER);

        btnGetNotif.addActionListener(e -> {
            try {
                Request req = new Request(Action.CLIENT_GET_NOTIFICATIONS);
                req.setToken(token);
                Response r = conn.send(req);
                if (!r.isOk()) {
                    Dialogs.error(r.getMessage());
                    return;
                }

                @SuppressWarnings("unchecked")
                List<String> list =
                        (List<String>) r.getData().get("notifications");

                notificationsArea.setText("");
                if (list == null || list.isEmpty()) {
                    notificationsArea.setText("Nicio notificare.\n");
                } else {
                    for (String s : list) {
                        notificationsArea.append(s + "\n");
                    }
                }
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });

        //FEEDBACK
        JPanel feedbackTab = new JPanel(new BorderLayout(8, 8));
        JButton btnFeedback =
                new JButton("Trimite feedback (selecteaza programare COMPLETED)");
        feedbackTab.add(btnFeedback, BorderLayout.NORTH);
        feedbackTab.add(
                new JLabel("Feedback se poate trimite doar dupa finalizarea lucrarii (COMPLETED)."),
                BorderLayout.CENTER
        );

        btnFeedback.addActionListener(e -> {
            Long id = getSelectedAppointmentId();
            if (id == null) {
                Dialogs.error("Selecteaza o programare din tabel.");
                return;
            }
            FeedbackDialog d =
                    new FeedbackDialog(conn, token, id, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
        });

        tabs.addTab("Programari", appointmentsTab);
        tabs.addTab("Notificari", notificationsTab);
        tabs.addTab("Feedback", feedbackTab);

        add(tabs, BorderLayout.CENTER);

        refreshAppointments();
    }

    private Long getSelectedAppointmentId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        if (v instanceof Long) return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();
        return Long.parseLong(v.toString());
    }

    private void refreshAppointments() {
        try {
            Request req = new Request(Action.CLIENT_LIST_APPOINTMENTS);
            req.setToken(token);
            Response r = conn.send(req);
            if (!r.isOk()) {
                Dialogs.error(r.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<Appointment> list =
                    (List<Appointment>) r.getData().get("appointments");

            model.setRowCount(0);
            if (list != null) {
                for (Appointment a : list) {
                    model.addRow(new Object[]{
                            a.getId(),
                            a.getServiceType(),
                            String.valueOf(a.getScheduledAt()),
                            a.getEstimatedCost(),
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
