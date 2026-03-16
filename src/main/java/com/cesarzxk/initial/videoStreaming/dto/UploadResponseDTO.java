package com.cesarzxk.initial.videoStreaming.dto;

import java.util.Map;

public class UploadResponseDTO {
    private String message;
    private Map<String, String> files;
    private Map<String, String> urls;

    public UploadResponseDTO() {}

    public UploadResponseDTO(String message, Map<String, String> files, Map<String, String> urls) {
        this.message = message;
        this.files = files;
        this.urls = urls;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public Map<String, String> getUrls() {
        return urls;
    }

    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

}