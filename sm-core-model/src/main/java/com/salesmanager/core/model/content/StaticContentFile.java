package com.salesmanager.core.model.content;

public abstract class StaticContentFile extends ContentFile {
    private static final long serialVersionUID = 1L;
    
    private FileContentType fileContentType;

    public FileContentType getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(FileContentType fileContentType) {
        this.fileContentType = fileContentType;
    }
}