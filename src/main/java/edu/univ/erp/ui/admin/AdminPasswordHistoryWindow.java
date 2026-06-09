package edu.univ.erp.ui.admin;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthenticationService;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class AdminPasswordHistoryWindow extends JDialog {
    private final AuthenticationService authService;
    private JTable historyTable;
    private HistoryTableModel tableModel;
    private JTextField searchField;
    private JLabel userLabel;
    public AdminPasswordHistoryWindow(Frame owner) {
        super(owner, "Admin: View User Password History", true);
        this.authService = new AuthenticationService();
        this.tableModel = new HistoryTableModel(Collections.emptyList());
        this.historyTable = new JTable(tableModel); // Initialize table here
        this.searchField = new JTextField(20);
        this.userLabel = new JLabel("Target User: Select or Search");
        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        if (!SessionManager.getInstance().isAdmin()) {
            JOptionPane.showMessageDialog(this, "Access Denied. Only Admins can view this", "Security Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JPanel searchPanel = new JPanel(new MigLayout("", "[grow, fill][80!][80!]", "[]"));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchPanel.add(userLabel, "span 3, wrap, gaptop 5, gapbottom 10");
        searchField.putClientProperty("JTextField.placeholderText", "Enter Username ");
        searchPanel.add(searchField, "growx");
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchUserAndLoadHistory());
        searchPanel.add(searchButton, "w 80!");
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        searchPanel.add(closeButton, "w 80!, wrap");
        add(searchPanel, BorderLayout.NORTH);
        historyTable.setRowHeight(25);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Password Hash History"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void searchUserAndLoadHistory() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username to search.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User targetUser = authService.findUserByUsername(query);
        if (targetUser != null) {
            userLabel.setText("Target User: " + targetUser.getUsername() + " (ID: " + targetUser.getUserid() + ", Role: " + targetUser.getRole() + ")");
            List<String> hashes = authService.getPasswordHistoryForAdmin(targetUser.getUserid());

            tableModel.setHistory(hashes);
            if (hashes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "User found, but no previous password history recorded.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            userLabel.setText("Target User: NOT FOUND");
            tableModel.setHistory(Collections.emptyList());
            JOptionPane.showMessageDialog(this, "User not found. Check the username.", "Search Failed", JOptionPane.WARNING_MESSAGE);
        }
    }


    // Custom Table Model for displaying password history hashes.
    private static class HistoryTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Index", "Password Hash", "Length"};
        private List<String> hashes; // Stores the raw hash strings

        public HistoryTableModel(List<String> hashes) {
            this.hashes = hashes;
        }

        public void setHistory(List<String> hashes) {
            this.hashes = hashes;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return hashes.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            String hash = hashes.get(row);
            return switch (col) {
                case 0 -> row + 1;
                case 1 -> hash;
                case 2 -> hash.length();
                default -> null;
            };
        }
    }
}