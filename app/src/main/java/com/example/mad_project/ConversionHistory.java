package com.example.mad_project;

public class ConversionHistory {
    private int id;
    private String userEmail;
    private String conversionType;
    private String originalFile;
    private String convertedFile;
    private long timestamp;

    public ConversionHistory(int id, String userEmail, String conversionType, 
                            String originalFile, String convertedFile, long timestamp) {
        this.id = id;
        this.userEmail = userEmail;
        this.conversionType = conversionType;
        this.originalFile = originalFile;
        this.convertedFile = convertedFile;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getConversionType() {
        return conversionType;
    }

    public String getOriginalFile() {
        return originalFile;
    }

    public String getConvertedFile() {
        return convertedFile;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFileName() {
        if (convertedFile != null && convertedFile.contains("/")) {
            return convertedFile.substring(convertedFile.lastIndexOf("/") + 1);
        }
        return convertedFile;
    }
}

