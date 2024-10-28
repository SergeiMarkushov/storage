package com.shemb.storage.repositories;

import com.shemb.storage.entities.FileAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileActionRepository extends JpaRepository<FileAction, Long> {
}
