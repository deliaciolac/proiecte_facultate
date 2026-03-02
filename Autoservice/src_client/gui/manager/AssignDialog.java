package gui.manager;

import dto.Action;
import dto.Request;
import dto.Response;
import model.User;
import network.ClientConnection;
import util.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AssignDialog extends JDialog {

    private final JComboBox<UserItem> mechanicsBox = new JComboBox<>();

    public AssignDialog(ClientConnection conn, String token, long appointmentId, Window owner) {
        super(owner, "Distribuie cerere (Appointment " + appointmentId + ")", ModalityType.APPLICATION_MODAL);
        setSize(520, 220);
        setLocationRelativeTo(owner);

        JLabel title = new JLabel("Selecteaza mecanicul disponibil:", SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel center = new JPanel(new GridLayout(2, 1, 8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        center.add(mechanicsBox);

        JButton refresh = new JButton("Refresh lista mecanici");
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshPanel.add(refresh);
        center.add(refreshPanel);

        JButton cancel = new JButton("Renunta");
        JButton assign = new JButton("Distribuie");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(assign);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        refresh.addActionListener(e -> loadMechanics(conn, token));

        assign.addActionListener(e -> {
            UserItem sel = (UserItem) mechanicsBox.getSelectedItem();
            if (sel == null) {
                Dialogs.error("Nu ai selectat niciun mecanic.");
                return;
            }

            try {
                Request req = new Request(Action.MANAGER_ASSIGN_TO_MECHANIC);
                req.setToken(token);
                req.getData().put("appointmentId", appointmentId);
                req.getData().put("mechanicId", sel.id);

                Response r = conn.send(req);
                if (!r.isOk()) {
                    Dialogs.error(r.getMessage());
                    return;
                }

                Dialogs.info("Distribuire OK.");
                dispose();

            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });


        loadMechanics(conn, token);
    }

    private void loadMechanics(ClientConnection conn, String token) {
        try {
            mechanicsBox.removeAllItems();

            Request req = new Request(Action.MANAGER_LIST_MECHANICS);
            req.setToken(token);

            Response r = conn.send(req);
            if (!r.isOk()) {
                Dialogs.error(r.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<User> mechs = (List<User>) r.getData().get("mechanics");

            if (mechs == null || mechs.isEmpty()) {
                Dialogs.error("Nu exista mecanici in sistem. (Admin trebuie sa promoveze un CLIENT la MECHANIC)");
                return;
            }

            for (User u : mechs) {
                mechanicsBox.addItem(new UserItem(u.getId(), u.getUsername()));
            }

            mechanicsBox.setSelectedIndex(0);

        } catch (Exception ex) {
            Dialogs.error("Eroare incarcare mecanici: " + ex.getMessage());
        }
    }

    private static class UserItem {
        final long id;
        final String username;

        UserItem(long id, String username) {
            this.id = id;
            this.username = username;
        }

        @Override
        public String toString() {
            return username + " (ID " + id + ")";
        }
    }
}