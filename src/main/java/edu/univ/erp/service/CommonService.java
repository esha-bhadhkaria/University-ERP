package edu.univ.erp.service;

import edu.univ.erp.data.NotificationDao;
import edu.univ.erp.data.TimeTableDao;
import edu.univ.erp.domain.Notification;
import edu.univ.erp.domain.TimeTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class CommonService {
    private final TimeTableDao timeTableDao = new TimeTableDao();
    private final NotificationDao notificationDao = new NotificationDao();

    //Retrieves the latest timetable and saves it to a temporary file for viewing.
    public File downloadLatestTimeTable() {
        TimeTable tt = timeTableDao.getLatestTimeTable();
        if (tt == null) return null;
        try {
            File tempFile = new File(System.getProperty("java.io.tmpdir"), tt.getFilename());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(tt.getFileData());
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Retrieves the latest global notification.
    public Notification getLatestNotification() {
        return notificationDao.getLatestNotification();
    }
}