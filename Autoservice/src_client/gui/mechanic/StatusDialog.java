package gui.mechanic;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class StatusDialog extends JDialog {

    private final JComboBox<String> status = new JComboBox<>(new String[]{"PENDING", "IN_PROGRESS", "COMPLETED"});

    public StatusDialog(ClientConnection conn, String token, long appointmentId, Window owner) {
        super(owner, "Update status (Appointment " + appointmentId + ")", ModalityType.APPLICATION_MODAL);
        setSize(420, 160);
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        p.add(new JLabel("Status"));
        p.add(status);

        JButton save = new JButton("Salveaza");
        JButton cancel = new JButton("Renunta");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(save);

        add(p, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            try {
                Request req = new Request(Action.MECHANIC_UPDATE_STATUS);
                req.setToken(token);
                req.getData().put("appointmentId", appointmentId);
                req.put("status", (String) status.getSelectedItem());

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Status actualizat.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}