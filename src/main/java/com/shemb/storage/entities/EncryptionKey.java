package com.shemb.storage.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shemb.storage.dtos.constants.TableNames;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(schema = TableNames.STORAGE_SCHEMA_NAME)
public class EncryptionKey implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @OneToOne
    @JoinColumn(name = "file_id")
    FileMetadata file;
    String encryptionKey;
}
