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
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(schema = TableNames.STORAGE_SCHEMA_NAME)
public class MyUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String username;
    String fullName;
    boolean active;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FileMetadata> files;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FileAction> actions;
    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    List<FileMetadata> allowedFiles;
}
