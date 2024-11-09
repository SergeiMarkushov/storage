package com.shemb.storage.services;

import com.shemb.storage.entities.FileMetadata;
import com.shemb.storage.entities.MyUser;
import com.shemb.storage.exceptions.UserNotFoundException;
import com.shemb.storage.repositories.MyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MyUserRepository repository;

    public List<FileMetadata> allowedFiles(String username) {
        return findByUsername(username).getAllowedFiles();
    }

    public MyUser findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с username " + username + " не найден"));
    }
}
