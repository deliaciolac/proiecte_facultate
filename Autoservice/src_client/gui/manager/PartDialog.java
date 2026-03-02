package gui.manager;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class PartDialog extends JDialog {

    private final JTextField name = new JTextField();
    private final JTextField stock = new JTextField("0");
    private final JTextField unitPrice = new JTextField("0");

    public PartDialog(ClientConnection conn, String token, Long partIdOrNull, Window owner) {
        super(owner, partIdOrNull == null ? "Adauga piesa" : "Editeaza piesa (ID " + partIdOrNull + ")", ModalityType.APPLICATION_MODAL);
        setSize(520, 220);
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        p.add(new JLabel("Nume piesa"));
        p.add(name);

        p.add(new JLabel("Stoc"));
        p.add(stock);

        p.add(new JLabel("Pret unit"));
        p.add(unitPrice);

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
                int st = Integer.parseInt(stock.getText().trim());
                double pr = Double.parseDouble(unitPrice.getText().trim());

                Request req = new Request(Action.MANAGER_UPSERT_PART);
                req.setToken(token);

                req.getData().put("id", partIdOrNull); // null => insert
                req.put("name", name.getText().trim());
                req.getData().put("stock", st);
                req.getData().put("unitPrice", pr);

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Piesa salvata.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}