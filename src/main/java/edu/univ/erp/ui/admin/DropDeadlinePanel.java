package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DropDeadlineDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

//Component Panel for Admin to set/edit the course drop deadline.
public class DropDeadlinePanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(DropDeadlinePanel.class);
    private final DropDeadlineDao deadlineDao;
    private final JTextField semesterField;
    private final JTextField yearField;
    private final JTextField dateField;

    public DropDeadlinePanel() {
        this.deadlineDao = new DropDeadlineDao();
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Set Course Drop Deadline (YYYY-MM-DD)", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(title, gbc);
        gbc.gridwidth = 1;

        // Semester
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        semesterField = new JTextField("1", 10);
        formPanel.add(semesterField, gbc);

        // Year
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        yearField = new JTextField(String.valueOf(LocalDate.now().getYear()), 10);
        formPanel.add(yearField, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Deadline Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        dateField = new JTextField(10);
        formPanel.add(dateField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton loadButton = new JButton("Load Existing");
        loadButton.addActionListener(e -> loadExistingDeadline());
        JButton saveButton = new JButton("Save Deadline");
        saveButton.addActionListener(e -> saveDeadline());

        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadExistingDeadline();
    }

    private void loadExistingDeadline() {
        try {
            int semester = Integer.parseInt(semesterField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());

            LocalDate deadline = deadlineDao.getDropDeadline(semester, year);

            if (deadline != null) {
                dateField.setText(deadline.format(DateTimeFormatter.ISO_LOCAL_DATE));
            } else {
                dateField.setText("");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Semester and Year must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveDeadline() {
        try {
            int semester = Integer.parseInt(semesterField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());
            String dateString = dateField.getText().trim();

            if (dateString.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Date cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate deadlineDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

            deadlineDao.setDropDeadline(semester, year, deadlineDate);

            JOptionPane.showMessageDialog(this,
                    String.format("Drop deadline saved successfully for S%d/%d.", semester, year),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Semester, Year must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            logger.error("DB Error saving deadline", e);
        }
    }
}