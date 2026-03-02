package gui.manager;

import dto.Action;
import dto.Request;
import dto.Response;
import model.Appointment;
import model.Part;
import model.FeedbackView;
import network.ClientConnection;
import util.Dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ManagerPanel extends JPanel {

    private final ClientConnection conn;
    private final String token;

    //CERERI
    private final DefaultTableModel pendingModel;
    private final JTable pendingTable;

    //INVENTAR
    private final DefaultTableModel partsModel;
    private final JTable partsTable;

    //PRETURI
    private final DefaultTableModel pricesModel;
    private final JTable pricesTable;

    //FEEDBACK
    private final DefaultTableModel feedbackModel;
    private final JTable feedbackTable;

    public ManagerPanel(ClientConnection conn, String token) {
        this.conn = conn;
        this.token = token;

        setLayout(new BorderLayout(10, 10));

        JTabbedPane tabs = new JTabbedPane();


        //CERERI + ASIGNARE
        JPanel pendingTab = new JPanel(new BorderLayout(8, 8));
        JPanel pendingTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshPending = new JButton("Refresh pending");
        JButton assign = new JButton("Distribuie la mecanic (selecteaza)");
        pendingTop.add(refreshPending);
        pendingTop.add(assign);

        pendingModel = new DefaultTableModel(new Object[]{"ID", "Tip", "Data/Ora", "Cost", "Vehicul"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingTable = new JTable(pendingModel);
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        pendingTab.add(pendingTop, BorderLayout.NORTH);
        pendingTab.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        refreshPending.addActionListener(e -> refreshPending());
        assign.addActionListener(e -> {
            Long id = selectedId(pendingTable, pendingModel);
            if (id == null) { Dialogs.error("Selecteaza o cerere."); return; }
            AssignDialog d = new AssignDialog(conn, token, id, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            refreshPending();
        });


        //INVENTAR
        JPanel invTab = new JPanel(new BorderLayout(8, 8));
        JPanel invTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshInv = new JButton("Refresh inventar");
        JButton upsert = new JButton("Adauga/editeaza piesa");
        invTop.add(refreshInv);
        invTop.add(upsert);

        partsModel = new DefaultTableModel(new Object[]{"ID", "Nume", "Stoc", "Pret unit"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        partsTable = new JTable(partsModel);
        partsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        invTab.add(invTop, BorderLayout.NORTH);
        invTab.add(new JScrollPane(partsTable), BorderLayout.CENTER);

        refreshInv.addActionListener(e -> refreshInventory());
        upsert.addActionListener(e -> {
            Long id = selectedId(partsTable, partsModel); // null => adaugare
            PartDialog d = new PartDialog(conn, token, id, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            refreshInventory();
        });

        //PRETURI (VIZUALIZARE + MODIFICARE)
        JPanel priceTab = new JPanel(new BorderLayout(8, 8));

        JPanel priceTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshPrices = new JButton("Vezi preturi");
        JButton setPrice = new JButton("Seteaza pret");
        priceTop.add(refreshPrices);
        priceTop.add(setPrice);

        pricesModel = new DefaultTableModel(new Object[]{"Tip serviciu", "Pret"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        pricesTable = new JTable(pricesModel);

        priceTab.add(priceTop, BorderLayout.NORTH);
        priceTab.add(new JScrollPane(pricesTable), BorderLayout.CENTER);

        refreshPrices.addActionListener(e -> refreshPrices());
        setPrice.addActionListener(e -> {
            PriceDialog d = new PriceDialog(conn, token, SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            refreshPrices();
        });


        //STATISTICI
        StatsPanel statsPanel = new StatsPanel(conn, token);


        //FEEDBACK
        JPanel feedbackTab = new JPanel(new BorderLayout(8, 8));
        JPanel feedbackTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshFeedback = new JButton("Refresh feedback");
        feedbackTop.add(refreshFeedback);

        feedbackModel = new DefaultTableModel(new Object[]{"Appointment", "Client", "Rating", "Comentariu", "Data"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        feedbackTable = new JTable(feedbackModel);
        feedbackTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        feedbackTab.add(feedbackTop, BorderLayout.NORTH);
        feedbackTab.add(new JScrollPane(feedbackTable), BorderLayout.CENTER);

        refreshFeedback.addActionListener(e -> refreshFeedback());

        tabs.addTab("Cereri", pendingTab);
        tabs.addTab("Inventar", invTab);
        tabs.addTab("Preturi", priceTab);
        tabs.addTab("Statistici", statsPanel);
        tabs.addTab("Feedback", feedbackTab);

        add(tabs, BorderLayout.CENTER);


        refreshPending();
        refreshInventory();
        refreshPrices();
        refreshFeedback();
    }

    private void refreshPending() {
        try {
            Request req = new Request(Action.MANAGER_LIST_PENDING);
            req.setToken(token);
            Response r = conn.send(req);
            if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

            @SuppressWarnings("unchecked")
            List<Appointment> list = (List<Appointment>) r.getData().get("appointments");

            pendingModel.setRowCount(0);
            if (list != null) {
                for (Appointment a : list) {
                    pendingModel.addRow(new Object[]{
                            a.getId(),
                            a.getServiceType(),
                            String.valueOf(a.getScheduledAt()),
                            a.getEstimatedCost(),
                            a.getVehicleInfo()
                    });
                }
            }
        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }

    private void refreshInventory() {
        try {
            Request req = new Request(Action.MANAGER_LIST_PARTS);
            req.setToken(token);
            Response r = conn.send(req);
            if (!r.isOk()) { Dialogs.error(r.getMessage()); return; }

            @SuppressWarnings("unchecked")
            List<Part> list = (List<Part>) r.getData().get("parts");

            partsModel.setRowCount(0);
            if (list != null) {
                for (Part p : list) {
                    partsModel.addRow(new Object[]{p.getId(), p.getName(), p.getStock(), p.getUnitPrice()});
                }
            }
        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }

    private void refreshPrices() {
        try {
            Request req = new Request(Action.MANAGER_LIST_PRICES);
            req.setToken(token);

            Response r = conn.send(req);
            if (!r.isOk()) {
                Dialogs.error(r.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            Map<Object, Object> prices = (Map<Object, Object>) r.getData().get("prices");

            pricesModel.setRowCount(0);
            if (prices != null) {
                for (Map.Entry<Object, Object> e : prices.entrySet()) {
                    pricesModel.addRow(new Object[]{String.valueOf(e.getKey()), e.getValue()});
                }
            }
        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }

    private void refreshFeedback() {
        try {
            Request req = new Request(Action.MANAGER_LIST_FEEDBACK);
            req.setToken(token);

            Response r = conn.send(req);
            if (!r.isOk()) {
                Dialogs.error(r.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<FeedbackView> list = (List<FeedbackView>) r.getData().get("feedback");

            feedbackModel.setRowCount(0);
            if (list != null) {
                for (FeedbackView f : list) {
                    feedbackModel.addRow(new Object[]{
                            f.getAppointmentId(),
                            f.getClientUsername(),
                            f.getRating(),
                            f.getComment(),
                            String.valueOf(f.getCreatedAt())
                    });
                }
            }
        } catch (Exception ex) {
            Dialogs.error("Eroare: " + ex.getMessage());
        }
    }

    private static Long selectedId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        if (v == null) return null;
        if (v instanceof Long) return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();
        return Long.parseLong(v.toString());
    }
}