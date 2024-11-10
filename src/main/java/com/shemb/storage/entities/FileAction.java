package com.shemb.storage.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shemb.storage.dtos.constants.TableNames;
import com.shemb.storage.dtos.enums.Action;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
//@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(schema = TableNames.STORAGE_SCHEMA_NAME, name = "file_actions")
public class FileAction implements Serializable {
    @Id
    @UuidGenerator
    String id;
    @OneToOne
    @JoinColumn(name = "file_id")
    FileMetadata file;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    MyUser user;
    Integer action;
    @CreationTimestamp
    OffsetDateTime timestamp;
    String details;

    public FileAction() {
    }

    public FileAction(String id, FileMetadata file, MyUser user, Integer action, OffsetDateTime timestamp, String details) {
        this.id = id;
        this.file = file;
        this.user = user;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FileMetadata getFile() {
        return file;
    }

    public void setFile(FileMetadata file) {
        this.file = file;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action.getIntValue();
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
