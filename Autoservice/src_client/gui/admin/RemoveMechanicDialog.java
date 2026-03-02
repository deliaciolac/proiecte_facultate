package gui.admin;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class RemoveMechanicDialog extends JDialog {

    public RemoveMechanicDialog(ClientConnection conn, String token, long mechanicId, Window owner) {
        super(owner, "Sterge mecanic (ID " + mechanicId + ")", ModalityType.APPLICATION_MODAL);
        setSize(420, 140);
        setLocationRelativeTo(owner);

        JLabel lbl = new JLabel("Stergi mecanicul ID=" + mechanicId + " ?", SwingConstants.CENTER);

        JButton delete = new JButton("Sterge");
        JButton cancel = new JButton("Renunta");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(delete);

        add(lbl, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        delete.addActionListener(e -> {
            try {
                Request req = new Request(Action.ADMIN_REMOVE_MECHANIC);
                req.setToken(token);
                req.getData().put("mechanicId", mechanicId);

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Mecanic sters.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}