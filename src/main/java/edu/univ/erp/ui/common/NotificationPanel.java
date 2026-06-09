package edu.univ.erp.ui.common;

import edu.univ.erp.domain.Notification;
import edu.univ.erp.service.CommonService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

//display the current global notification message.
public class NotificationPanel extends JPanel {
    private final CommonService commonService;
    public final JTextArea messageArea;
    private final JLabel metadataLabel;
    private static final Color ALERT_COLOR = new Color(224, 247, 250); // Light Sky Blue
    private static final Color BORDER_COLOR = new Color(0, 188, 212); // Cyan/Deep Sky Blue for border

    public NotificationPanel() {
        this.commonService = new CommonService();
        setLayout(new MigLayout("insets 5, wrap 1", "[200, grow, fill]", "[]push[]"));
        setBackground(ALERT_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        metadataLabel = new JLabel("System Notification", SwingConstants.CENTER);
        metadataLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(metadataLabel);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setBackground(ALERT_COLOR); // Apply custom color
        messageArea.setForeground(Color.BLACK);
        messageArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageArea.setBorder(null);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        scrollPane.setBorder(null);

        add(scrollPane, "grow, pushy");

        loadNotification();
    }

    public void loadNotification() {
        Notification notification = commonService.getLatestNotification();

        if (notification == null || notification.getMessage().trim().isEmpty()) {
            messageArea.setText("No active system announcements.");
            metadataLabel.setText("System Status");
            this.setVisible(false);
        } else {
            messageArea.setText(notification.getMessage());
            String postedBy = notification.getPostedByUsername() != null ? notification.getPostedByUsername() : "System Admin";
            String postedTime = notification.getPostedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
            metadataLabel.setText(" ANNOUNCEMENT by " + postedBy + " at " + postedTime);

            this.setVisible(true);
        }
    }
}