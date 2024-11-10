package com.shemb.storage.dtos.dto;

import com.shemb.storage.entities.MyUser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileMetadataDto {
    Long id;
    MyUser user;
    String originFileName;
    String uniqueFileName;
    Long fileSize;
    String fileType;
    String category;
    OffsetDateTime uploadDate;

    public FileMetadataDto() {
    }

    public FileMetadataDto(Long id, MyUser user, String originFileName, String uniqueFileName, Long fileSize, String fileType, String category, OffsetDateTime uploadDate) {
        this.id = id;
        this.user = user;
        this.originFileName = originFileName;
        this.uniqueFileName = uniqueFileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.category = category;
        this.uploadDate = uploadDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public String getOriginFileName() {
        return originFileName;
    }

    public void setOriginFileName(String originFileName) {
        this.originFileName = originFileName;
    }

    public String getUniqueFileName() {
        return uniqueFileName;
    }

    public void setUniqueFileName(String uniqueFileName) {
        this.uniqueFileName = uniqueFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public OffsetDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(OffsetDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
