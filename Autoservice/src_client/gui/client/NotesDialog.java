package gui.client;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class NotesDialog extends JDialog {

    private final JTextArea extraNotes = new JTextArea(7, 40);

    public NotesDialog(ClientConnection conn, String token, long appointmentId, Window owner) {
        super(owner, "Adauga observatii (Appointment " + appointmentId + ")", ModalityType.APPLICATION_MODAL);
        setSize(520, 260);
        setLocationRelativeTo(owner);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        center.add(new JLabel("Observatii suplimentare:"), BorderLayout.NORTH);
        center.add(new JScrollPane(extraNotes), BorderLayout.CENTER);

        JButton save = new JButton("Salveaza");
        JButton cancel = new JButton("Renunta");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(save);

        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            try {
                Request req = new Request(Action.CLIENT_ADD_NOTES);
                req.setToken(token);
                req.getData().put("appointmentId", appointmentId);
                req.put("extraNotes", extraNotes.getText().trim());

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Observatii salvate.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}