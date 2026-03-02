package gui.mechanic;

import dto.Action;
import dto.Request;
import dto.Response;
import model.Part;
import model.UsedPart;
import network.ClientConnection;
import util.Dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PartsDurationDialog extends JDialog {

    private final ClientConnection conn;
    private final String token;
    private final long appointmentId;

    private final JTextField durationField = new JTextField("60");
    private final JTextArea mechanicNotes = new JTextArea(4, 30);

    private final JComboBox<PartItem> partBox = new JComboBox<>();
    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private final DefaultTableModel model;
    private final JTable table;

    public PartsDurationDialog(ClientConnection conn, String token, long appointmentId, Window owner) {
        super(owner, "Piese + durata (Appointment " + appointmentId + ")", ModalityType.APPLICATION_MODAL);
        this.conn = conn;
        this.token = token;
        this.appointmentId = appointmentId;

        setSize(780, 520);
        setLocationRelativeTo(owner);


        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; top.add(new JLabel("Durata (minute)"), gc);
        gc.gridx = 1; gc.gridy = 0; top.add(durationField, gc);

        gc.gridx = 0; gc.gridy = 1; top.add(new JLabel("Note mecanic"), gc);
        gc.gridx = 1; gc.gridy = 1;
        JScrollPane notesScroll = new JScrollPane(mechanicNotes);
        top.add(notesScroll, gc);

        JPanel picker = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshInv = new JButton("Refresh inventar");
        JButton addPart = new JButton("Adauga piesa selectata");
        JButton removeRow = new JButton("Sterge rand selectat");

        picker.add(new JLabel("Piesa:"));
        picker.add(partBox);
        picker.add(new JLabel("Cantitate:"));
        picker.add(qtySpinner);
        picker.add(addPart);
        picker.add(removeRow);
        picker.add(refreshInv);

        model = new DefaultTableModel(new Object[]{"InventoryID", "Nume piesa", "Cantitate"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton cancel = new JButton("Renunta");
        JButton save = new JButton("Salveaza");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancel);
        bottom.add(save);

        setLayout(new BorderLayout(8, 8));
        add(top, BorderLayout.NORTH);
        add(picker, BorderLayout.CENTER);
        add(new JScrollPane(table), BorderLayout.SOUTH);
        add(bottom, BorderLayout.PAGE_END);


        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.add(picker, BorderLayout.NORTH);
        centerWrap.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);


        cancel.addActionListener(e -> dispose());

        refreshInv.addActionListener(e -> loadInventory());

        addPart.addActionListener(e -> {
            PartItem sel = (PartItem) partBox.getSelectedItem();
            if (sel == null) {
                Dialogs.error("Inventar gol. (Manager trebuie sa adauge piese in inventar)");
                return;
            }
            int qty = (Integer) qtySpinner.getValue();
            model.addRow(new Object[]{sel.id, sel.name, qty});
        });

        removeRow.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            model.removeRow(row);
        });

        save.addActionListener(e -> save());


        loadInventory();
    }

    private void loadInventory() {
        try {
            partBox.removeAllItems();

            Request req = new Request(Action.MECHANIC_LIST_PARTS);
            req.setToken(token);

            Response r = conn.send(req);
            if (!r.isOk()) {
                Dialogs.error(r.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<Part> parts = (List<Part>) r.getData().get("parts");

            if (parts == null || parts.isEmpty()) {
                Dialogs.error("Inventarul este gol. Adauga piese din MANAGER -> Inventar.");
                return;
            }

            for (Part p : parts) {
                partBox.addItem(new PartItem(p.getId(), p.getName(), p.getStock(), p.getUnitPrice()));
            }
            partBox.setSelectedIndex(0);

        } catch (Exception ex) {
            Dialogs.error("Eroare inventar: " + ex.getMessage());
        }
    }

    private void save() {
        try {
            int duration;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
            } catch (Exception e) {
                Dialogs.error("Durata invalida.");
                return;
            }
            if (duration < 0) {
                Dialogs.error("Durata invalida.");
                return;
            }

            String notes = mechanicNotes.getText();

            List<UsedPart> partsUsed = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                long invId = Long.parseLong(model.getValueAt(i, 0).toString());
                String name = model.getValueAt(i, 1).toString();
                int qty = Integer.parseInt(model.getValueAt(i, 2).toString());

                UsedPart up = new UsedPart();
                up.setPartId(invId);
                up.setPartName(name);
                up.setQuantity(qty);
                partsUsed.add(up);
            }

            Request req = new Request(Action.MECHANIC_ADD_PARTS_AND_DURATION);
            req.setToken(token);
            req.getData().put("appointmentId", appointmentId);
            req.getData().put("durationMinutes", duration);
            req.put("mechanicNotes", notes);
            req.getData().put("partsUsed", partsUsed);

            Response r = conn.send(req);
            if (!r.isOk()) {
                Dialogs.error(r.getMessage());
                return;
            }

            Dialogs.info("Salvat.");
            dispose();

        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }

    private static class PartItem {
        final long id;
        final String name;
        final int stock;
        final double price;

        PartItem(long id, String name, int stock, double price) {
            this.id = id;
            this.name = name;
            this.stock = stock;
            this.price = price;
        }

        @Override
        public String toString() {
            return name + " (stoc " + stock + ", " + price + " lei)";
        }
    }
}