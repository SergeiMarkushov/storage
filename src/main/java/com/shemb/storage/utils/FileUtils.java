package com.shemb.storage.utils;

import com.shemb.storage.dtos.dto.StorageInfo;
import com.shemb.storage.exceptions.FileProcessingException;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@UtilityClass
public class FileUtils {
    public Path createDir(String username) {
        String userHome = System.getProperty("user.home");
        Path userDir = Paths.get(userHome, "uploads", username);
        try {
            if (Files.notExists(userDir)) {
                Files.createDirectories(userDir);
            }
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка создания директории пользователя");
        }
        return userDir;
    }

    public void upload(MultipartFile[] files, String username) {
        for (MultipartFile file : files) {
            try {
                Path path = createDir(username).resolve(Objects.requireNonNull(file.getOriginalFilename()));
                Files.write(path, file.getBytes());
            } catch (IOException e) {
                throw new FileProcessingException("Ошибка сохранения файла");
            }
        }
    }

    public Resource download(String filename, String username) {
        try {
            Path path = createDir(username).resolve(Objects.requireNonNull(filename));
            return new UrlResource(path.toUri());
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка скачивания файла");
        }
    }

    public void delete(String filename, String username) {
        try {
            Path path = createDir(username).resolve(Objects.requireNonNull(filename));
            if (Files.exists(path)) {
                Files.delete(path);
            } else {
                throw new FileProcessingException("Файл не найден");
            }
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка удаления файла");
        }
    }

    public StorageInfo getStorageInfo() {
        try {
            Path path = FileSystems.getDefault().getPath("/");
            FileStore store = Files.getFileStore(path);
            long totalSpace = store.getTotalSpace();
            long usableSpace = store.getUsableSpace();
            return new StorageInfo(totalSpace, usableSpace);
        } catch (Exception e) {
            e.printStackTrace();
            return new StorageInfo(0, 0);
        }
    }
}
