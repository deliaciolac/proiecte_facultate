package gui.admin;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class AddMechanicDialog extends JDialog {
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();

    public AddMechanicDialog(ClientConnection conn, String token, Window owner) {
        super(owner, "Adauga mecanic", ModalityType.APPLICATION_MODAL);
        setSize(480, 220);
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        p.add(new JLabel("Username"));
        p.add(username);

        p.add(new JLabel("Password"));
        p.add(password);

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
                Request req = new Request(Action.ADMIN_ADD_MECHANIC);
                req.setToken(token);
                req.put("username", username.getText().trim());
                req.put("password", new String(password.getPassword()));

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Mecanic adaugat.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}