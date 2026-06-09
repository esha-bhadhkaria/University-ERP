package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class TimeTable {
    private int id;
    private String filename;
    private byte[] fileData;
    private LocalDateTime uploadDate;

    public TimeTable() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
}