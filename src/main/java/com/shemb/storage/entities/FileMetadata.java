package com.shemb.storage.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shemb.storage.dtos.constants.TableNames;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(schema = TableNames.STORAGE_SCHEMA_NAME)
public class FileMetadata implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    MyUser user;
    String fileName;
    Integer fileSize;
    String fileType;
    @CreationTimestamp
    OffsetDateTime uploadDate;
}
