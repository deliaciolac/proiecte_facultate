package gui.admin;

import dto.Action;
import dto.Request;
import dto.Response;
import model.User;
import network.ClientConnection;
import util.Dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {

    private final ClientConnection conn;
    private final String token;

    private final DefaultTableModel model;
    private final JTable table;

    public AdminPanel(ClientConnection conn, String token) {
        this.conn = conn;
        this.token = token;

        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnRefresh = new JButton("Refresh users");
        JButton btnAdd = new JButton("Adauga mecanic");
        JButton btnEdit = new JButton("Editeaza mecanic");
        JButton btnDelete = new JButton("Sterge mecanic");

        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);

        model = new DefaultTableModel(new Object[]{
                "ID", "Username", "Role"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refreshUsers());

        // ADAUGARE MECANIC
        btnAdd.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Username mecanic:");
            if (username == null || username.isBlank()) return;

            String password = JOptionPane.showInputDialog(this, "Parola:");
            if (password == null || password.isBlank()) return;

            try {
                Request req = new Request(Action.ADMIN_ADD_MECHANIC);
                req.setToken(token);
                req.getData().put("username", username);
                req.getData().put("password", password);

                Response r = conn.send(req);
                if (!r.isOk()) {
                    Dialogs.error(r.getMessage());
                    return;
                }

                refreshUsers();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });


        // EDITARE MECANIC
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                Dialogs.error("Selecteaza un mecanic.");
                return;
            }

            String role = model.getValueAt(row, 2).toString();
            if (!"MECHANIC".equals(role)) {
                Dialogs.error("Se pot edita doar utilizatori MECHANIC.");
                return;
            }

            long id = Long.parseLong(model.getValueAt(row, 0).toString());

            JTextField tfUser = new JTextField();
            JPasswordField tfPass = new JPasswordField();

            Object[] msg = {
                    "Username nou (optional):", tfUser,
                    "Parola noua (optional):", tfPass
            };

            int ok = JOptionPane.showConfirmDialog(
                    this, msg, "Editare mecanic", JOptionPane.OK_CANCEL_OPTION
            );
            if (ok != JOptionPane.OK_OPTION) return;

            String newUsername = tfUser.getText().trim();
            String newPassword = new String(tfPass.getPassword()).trim();

            if (newUsername.isEmpty() && newPassword.isEmpty()) {
                Dialogs.error("Nu ai modificat nimic.");
                return;
            }

            try {
                Request req = new Request(Action.ADMIN_EDIT_MECHANIC);
                req.setToken(token);
                req.getData().put("mechanicId", id);
                if (!newUsername.isEmpty()) req.getData().put("newUsername", newUsername);
                if (!newPassword.isEmpty()) req.getData().put("newPassword", newPassword);

                Response r = conn.send(req);
                if (!r.isOk()) {
                    Dialogs.error(r.getMessage());
                    return;
                }

                refreshUsers();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });


        // STERGERE MECANIC
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                Dialogs.error("Selecteaza un mecanic.");
                return;
            }

            String role = model.getValueAt(row, 2).toString();
            if (!"MECHANIC".equals(role)) {
                Dialogs.error("Se pot sterge doar utilizatori MECHANIC.");
                return;
            }

            int ok = JOptionPane.showConfirmDialog(
                    this,
                    "Sigur doresti sa stergi mecanicul?",
                    "Confirmare",
                    JOptionPane.YES_NO_OPTION
            );
            if (ok != JOptionPane.YES_OPTION) return;

            long id = Long.parseLong(model.getValueAt(row, 0).toString());

            try {
                Request req = new Request(Action.ADMIN_REMOVE_MECHANIC);
                req.setToken(token);
                req.getData().put("mechanicId", id);

                Response r = conn.send(req);
                if (!r.isOk()) {
                    Dialogs.error(r.getMessage());
                    return;
                }

                refreshUsers();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });

        refreshUsers();
    }

    private void refreshUsers() {
        try {
            Request req = new Request(Action.ADMIN_LIST_USERS);
            req.setToken(token);
            Response r = conn.send(req);
            if (!r.isOk()) {
                Dialogs.error(r.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<User> list = (List<User>) r.getData().get("users");

            model.setRowCount(0);
            if (list != null) {
                for (User u : list) {
                    model.addRow(new Object[]{
                            u.getId(),
                            u.getUsername(),
                            u.getRole()
                    });
                }
            }
        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }
}
