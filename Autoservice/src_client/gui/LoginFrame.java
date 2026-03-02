package gui;

import config.ClientConfig;
import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();

    public LoginFrame() {
        setTitle("AutoService - Login");
        setSize(420, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        form.add(new JLabel("Username"));
        form.add(username);

        form.add(new JLabel("Password"));
        form.add(password);

        JButton login = new JButton("Login");
        JButton regClient = new JButton("Create CLIENT account");

        //crearea contului e posibila numai pt client
        JPanel btns = new JPanel(new GridLayout(1, 2, 8, 8));
        btns.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        btns.add(login);
        btns.add(regClient);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        login.addActionListener(e -> auth(Action.LOGIN));
        regClient.addActionListener(e -> auth(Action.REGISTER_CLIENT));

        setVisible(true);
    }

    private void auth(Action action) {
        String u = username.getText().trim();
        String p = new String(password.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            Dialogs.error("Completeaza username si parola.");
            return;
        }

        try {
            ClientConnection conn = new ClientConnection(ClientConfig.HOST, ClientConfig.PORT);

            Request req = new Request(action)
                    .put("username", u)
                    .put("password", p);

            Response r = conn.send(req);
            if (!r.isOk()) {
                conn.close();
                Dialogs.error(r.getMessage());
                return;
            }

            String token = r.getToken();
            String role = (String) r.getData().get("role");
            if (token == null || role == null) {
                conn.close();
                Dialogs.error("Server nu a trimis token/rol.");
                return;
            }

            dispose();
            new MainFrame(conn, token, role);

        } catch (Exception ex) {
            Dialogs.error("Eroare conectare: " + ex.getMessage());
        }
    }
}