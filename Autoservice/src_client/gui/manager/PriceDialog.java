package gui.manager;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class PriceDialog extends JDialog {
    private final JComboBox<String> type = new JComboBox<>(new String[]{"REVISION", "REPAIR"});
    private final JTextField price = new JTextField("450.0");

    public PriceDialog(ClientConnection conn, String token, Window owner) {
        super(owner, "Seteaza pret serviciu", ModalityType.APPLICATION_MODAL);
        setSize(460, 180);
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        p.add(new JLabel("ServiceType"));
        p.add(type);

        p.add(new JLabel("Pret"));
        p.add(price);

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
                double pr = Double.parseDouble(price.getText().trim());

                Request req = new Request(Action.MANAGER_SET_SERVICE_PRICE);
                req.setToken(token);
                req.put("serviceType", (String) type.getSelectedItem());
                req.getData().put("price", pr);

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Pret salvat.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}