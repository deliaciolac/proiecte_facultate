package gui.admin;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class EditMechanicDialog extends JDialog {

    private final JTextField newUsername = new JTextField();

    public EditMechanicDialog(ClientConnection conn, String token, long mechanicId, Window owner) {
        super(owner, "Editeaza mecanic (ID " + mechanicId + ")", ModalityType.APPLICATION_MODAL);
        setSize(520, 180);
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        p.add(new JLabel("Noul username"));
        p.add(newUsername);

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
                Request req = new Request(Action.ADMIN_EDIT_MECHANIC);
                req.setToken(token);
                req.getData().put("mechanicId", mechanicId);
                req.put("newUsername", newUsername.getText().trim());

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Mecanic editat.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}