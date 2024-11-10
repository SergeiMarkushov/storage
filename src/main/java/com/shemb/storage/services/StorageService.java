package com.shemb.storage.services;

import com.shemb.storage.dtos.dto.StorageInfo;
import com.shemb.storage.entities.EncryptionKey;
import com.shemb.storage.entities.FileAction;
import com.shemb.storage.entities.FileMetadata;
import com.shemb.storage.entities.MyUser;
import com.shemb.storage.dtos.enums.Action;
import com.shemb.storage.exceptions.FileProcessingException;
import com.shemb.storage.repositories.EncryptionKeyRepository;
import com.shemb.storage.repositories.FileActionRepository;
import com.shemb.storage.repositories.FileMetadataRepository;
import com.shemb.storage.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.util.List;

import static com.shemb.storage.utils.FileUtils.getCategory;
import static com.shemb.storage.utils.Utils.bytesToKey;
import static com.shemb.storage.utils.Utils.generateKey;
import static com.shemb.storage.utils.Utils.getUuid;
import static com.shemb.storage.utils.Utils.keyToBytes;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final UserService userService;
    private final FileMetadataRepository fileMetadataRepository;
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final FileActionRepository fileActionRepository;

    public List<FileMetadata> getAllFiles(String username) {
        return fileMetadataRepository.findAllByUser(userService.findByUsername(username));
    }

    /**
     * Метод в разработке, общий концепт такой:
     * достаем файлы, у которых владелец пользователь
     * достаем файлы, к которым предоставлен доступ пользователю.
     * возвращаем на фронт общий список этих файлов
     */
    public List<FileMetadata> getAllFilesAndAllowed(String username) {
        List<FileMetadata> allFiles = getAllFiles(username);
        List<FileMetadata> allowedFiles = userService.allowedFiles(username);
        allFiles.addAll(allowedFiles);
        return allFiles;
    }

    public void uploadFile(MultipartFile[] files, String username) {
        MyUser user = userService.findByUsername(username);
        for (MultipartFile file : files) {
            String uuid = getUuid();
            String uniqueFileName = file.getOriginalFilename() + "_" + uuid;
            FileMetadata fileMetadata = saveFileMetadata(file, user, uniqueFileName);
            uploadFile(file, fileMetadata, username, uniqueFileName);
        }
    }

    public Resource download(String filename, String username, HttpServletRequest request) {
        FileMetadata fileMetadata = fileMetadataRepository.findByUniqueFileName(filename)
                .orElseThrow(() -> new FileProcessingException("Файл с именем " + filename + " не найден"));
        SecretKey key = bytesToKey(encryptionKeyRepository.findByFile(fileMetadata).getEncryptionKey());
        return FileUtils.download(fileMetadata.getUniqueFileName(), username, request, key);
    }

    private FileMetadata saveFileMetadata(MultipartFile file, MyUser user, String uniqueFileName) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setUser(user);
        fileMetadata.setOriginFileName(file.getOriginalFilename());
        fileMetadata.setUniqueFileName(uniqueFileName);
        fileMetadata.setFileSize(file.getSize());
        fileMetadata.setFileType(file.getContentType());
        fileMetadata.setCategory(getCategory(file.getOriginalFilename()).getIntValue());
        fileMetadataRepository.save(fileMetadata);
        saveFileAction(fileMetadata, user, Action.SAVE, "Сохранен файл");
        return fileMetadata;
    }

    private void uploadFile(MultipartFile file, FileMetadata fileMetadata, String username, String uniqueFileName) {
        SecretKey key = generateKey();
        FileUtils.upload(file, username, key, uniqueFileName);
        saveEncryptionKey(fileMetadata, key);
    }

    private void saveEncryptionKey(FileMetadata file, SecretKey key) {
        EncryptionKey encryptionKey = new EncryptionKey();
        encryptionKey.setFile(file);
        encryptionKey.setEncryptionKey(keyToBytes(key));
        encryptionKeyRepository.save(encryptionKey);
    }

    private void saveFileAction(FileMetadata file, MyUser user, Action action, String details) {
        FileAction fileAction = new FileAction();
        fileAction.setFile(file);
        fileAction.setUser(user);
        fileAction.setAction(action);
        fileAction.setDetails(details);
        fileActionRepository.save(fileAction);
    }

    public StorageInfo getStorageInfo() {
        return FileUtils.getStorageInfo();
    }
}
