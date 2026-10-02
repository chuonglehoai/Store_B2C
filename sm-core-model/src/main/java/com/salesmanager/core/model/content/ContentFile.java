package com.salesmanager.core.model.content;

import java.io.Serializable;

public abstract class ContentFile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String fileName;
    private String mimeType;

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public String getMimeType() {
        return mimeType;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }
}