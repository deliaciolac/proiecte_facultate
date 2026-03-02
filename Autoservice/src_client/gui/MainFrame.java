package gui;

import gui.admin.AdminPanel;
import network.ClientConnection;
import gui.client.ClientPanel;
import gui.manager.ManagerPanel;
import gui.mechanic.MechanicPanel;
import util.Dialogs;
import dto.Action;
import dto.Request;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final ClientConnection conn;
    private final String token;
    private final String role;

    public MainFrame(ClientConnection conn, String token, String role) {
        this.conn = conn;
        this.token = token;
        this.role = role;

        setTitle("AutoService - " + role);
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("Logged as: " + role, SwingConstants.LEFT);
        JButton logout = new JButton("Logout");

        logout.addActionListener(e -> doLogout());

        top.add(lbl, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        JComponent center = switch (role) {
            case "CLIENT" -> new ClientPanel(conn, token);
            case "MECHANIC" -> new MechanicPanel(conn, token);
            case "MANAGER" -> new ManagerPanel(conn, token);
            case "ADMIN" -> new AdminPanel(conn, token);
            default -> {
                Dialogs.error("Rol necunoscut: " + role);
                yield new JPanel();
            }
        };

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        setVisible(true);
    }

    private void doLogout() {
        try {
            Request req = new Request(Action.LOGOUT);
            req.setToken(token);
            conn.send(req);
        } catch (Exception ignored) {}
        try { conn.close(); } catch (Exception ignored) {}
        dispose();
        new LoginFrame();
    }
}