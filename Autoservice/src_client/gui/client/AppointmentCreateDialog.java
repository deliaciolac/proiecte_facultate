package gui.client;

import network.ClientConnection;
import util.Dialogs;
import dto.Action;
import dto.Request;
import dto.Response;

import javax.swing.*;
import java.awt.*;

public class AppointmentCreateDialog extends JDialog {

    private final JComboBox<String> serviceType = new JComboBox<>(new String[]{"REVISION", "REPAIR"});
    private final JTextField dateTime = new JTextField("2026-01-08T18:30");
    private final JTextField vehicle = new JTextField();
    private final JTextArea notes = new JTextArea(5, 30);

    public AppointmentCreateDialog(ClientConnection conn, String token, Window owner) {
        super(owner, "Creeaza programare", ModalityType.APPLICATION_MODAL);
        setSize(520, 360);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(new JLabel("Tip serviciu"));
        form.add(serviceType);

        form.add(new JLabel("Data/Ora (YYYY-MM-DDTHH:MM)"));
        form.add(dateTime);

        form.add(new JLabel("Info vehicul"));
        form.add(vehicle);

        form.add(new JLabel("Observatii"));
        JScrollPane sp = new JScrollPane(notes);

        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);

        JButton save = new JButton("Salveaza");
        JButton cancel = new JButton("Renunta");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(save);

        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());

        save.addActionListener(e -> {
            try {
                Request req = new Request(Action.CLIENT_CREATE_APPOINTMENT);
                req.setToken(token);
                req.put("serviceType", (String) serviceType.getSelectedItem());
                req.put("scheduledAt", dateTime.getText().trim());
                req.put("vehicleInfo", vehicle.getText().trim());
                req.put("notes", notes.getText().trim());

                Response r = conn.send(req);
                if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

                Dialogs.info("Programare creata.");
                dispose();
            } catch (Exception ex) {
                Dialogs.error("Eroare: " + ex.getMessage());
            }
        });
    }
}