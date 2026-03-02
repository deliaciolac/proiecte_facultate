package gui.client;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class FeedbackDialog extends JDialog {

    private final JSpinner rating = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
    private final JTextArea comment = new JTextArea(6, 40);

    public FeedbackDialog(ClientConnection conn, String token, long appointmentId, Window owner) {
        super(owner, "Feedback (Appointment " + appointmentId + ")", ModalityType.APPLICATION_MODAL);
        setSize(520, 300);
        setLocationRelativeTo(owner);

        JPanel top = new JPanel(new GridLayout(0, 2, 8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        top.add(new JLabel("Rating (1..5)"));
        top.add(rating);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        center.add(new JLabel("Comentariu:"), BorderLayout.NORTH);
        center.add(new JScrollPane(comment), BorderLayout.CENTER);

        JButton send = new JButton("Trimite");
        JButton cancel = new JButton("Renunta");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(send);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        send.addActionListener(e -> {
            try {
                Request req = new Request(Action.CLIENT_SUBMIT_FEEDBACK);
                req.setToken(token);
                req.getData().put("appointmentId", appointmentId);
                req.getData().put("rating", (Integer) rating.getValue());
                req.put("comment", comment.getText().trim());

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Feedback trimis.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}