package com.shemb.storage.repositories;

import com.shemb.storage.entities.FileMetadata;
import com.shemb.storage.entities.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findAllByUserAndDeleted(MyUser user, Boolean deleted);

    Optional<FileMetadata> findByUniqueFileName(String fileName);
}
