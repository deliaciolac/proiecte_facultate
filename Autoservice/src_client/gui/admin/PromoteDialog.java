package gui.admin;

import dto.Action;
import dto.Request;
import dto.Response;
import network.ClientConnection;
import util.Dialogs;

import javax.swing.*;
import java.awt.*;

public class PromoteDialog extends JDialog {

    public PromoteDialog(ClientConnection conn, String token, long clientId, Window owner) {
        super(owner, "Promovare (CLIENT ID " + clientId + ")", ModalityType.APPLICATION_MODAL);
        setSize(520, 200);
        setLocationRelativeTo(owner);

        JLabel title = new JLabel("Selecteaza rolul nou pentru CLIENT ID=" + clientId, SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"MECHANIC", "MANAGER"});

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        center.add(new JLabel("Rol nou:"));
        center.add(roleBox);

        JButton apply = new JButton("Aplica");
        JButton cancel = new JButton("Renunta");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(apply);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        apply.addActionListener(e -> {
            try {
                String target = (String) roleBox.getSelectedItem();
                if (target == null) return;

                Action action = target.equals("MANAGER")
                        ? Action.ADMIN_PROMOTE_CLIENT_TO_MANAGER
                        : Action.ADMIN_PROMOTE_CLIENT_TO_MECHANIC;

                Request req = new Request(action);
                req.setToken(token);
                req.getData().put("clientId", clientId);

                Response r = conn.send(req);
                if (!r.isOk()) {
                    Dialogs.error(r.getMessage());
                    return;
                }

                Dialogs.info("Promovare reusita: CLIENT -> " + target);
                dispose();

            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}