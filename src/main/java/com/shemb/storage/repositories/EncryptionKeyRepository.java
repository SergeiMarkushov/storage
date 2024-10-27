package com.shemb.storage.repositories;

import com.shemb.storage.entities.EncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EncryptionKeyRepository extends JpaRepository<EncryptionKey, Long> {
}
