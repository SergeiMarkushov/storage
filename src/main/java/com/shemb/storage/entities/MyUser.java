package com.shemb.storage.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shemb.storage.dtos.constants.TableNames;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Entity
//@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(schema = TableNames.STORAGE_SCHEMA_NAME, name = "my_users")
public class MyUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String username;
    String fullName;
    boolean isActive;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FileMetadata> files;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FileAction> actions;
    @ManyToMany
    @JoinTable(name = "allowed_files",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    List<FileMetadata> allowedFiles;

    public MyUser() {
    }

    public MyUser(Long id, String username, String fullName, boolean isActive, List<FileMetadata> files, List<FileAction> actions, List<FileMetadata> allowedFiles) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.isActive = isActive;
        this.files = files;
        this.actions = actions;
        this.allowedFiles = allowedFiles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public List<FileMetadata> getFiles() {
        return files;
    }

    public void setFiles(List<FileMetadata> files) {
        this.files = files;
    }

    public List<FileAction> getActions() {
        return actions;
    }

    public void setActions(List<FileAction> actions) {
        this.actions = actions;
    }

    public List<FileMetadata> getAllowedFiles() {
        return allowedFiles;
    }

    public void setAllowedFiles(List<FileMetadata> allowedFiles) {
        this.allowedFiles = allowedFiles;
    }
}
