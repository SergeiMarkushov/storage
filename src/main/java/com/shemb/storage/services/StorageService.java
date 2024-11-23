package com.shemb.storage.services;

import com.shemb.storage.dtos.dto.StorageInfo;
import com.shemb.storage.dtos.enums.Action;
import com.shemb.storage.entities.EncryptionKey;
import com.shemb.storage.entities.FileAction;
import com.shemb.storage.entities.FileMetadata;
import com.shemb.storage.entities.MyUser;
import com.shemb.storage.exceptions.FileProcessingException;
import com.shemb.storage.repositories.EncryptionKeyRepository;
import com.shemb.storage.repositories.FileActionRepository;
import com.shemb.storage.repositories.FileMetadataRepository;
import com.shemb.storage.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

import static com.shemb.storage.utils.FileUtils.getCategory;
import static com.shemb.storage.utils.FileUtils.isScanFile;
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
    private final VirusScannerService virusScanner;

    public List<FileMetadata> getAllFiles(String username) {
        return fileMetadataRepository.findAllByUserAndDeleted(userService.findByUsername(username), false);
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

    public List<String> uploadFile(MultipartFile[] files, String username) {
        MyUser user = userService.findByUsername(username);
        List<String> infectedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (isScanFile(file.getOriginalFilename())) {
                if (!virusScanner.isFileInfected(file, username)) {
                    infectedFiles.add(file.getOriginalFilename());
                } else {
                    uploadFile(file, user);
                }
            } else {
                uploadFile(file, user);
            }
        }
        return infectedFiles;
    }

    public Resource download(String filename, String username) {
        FileMetadata fileMetadata = getFileMetadata(filename);
        SecretKey key = bytesToKey(encryptionKeyRepository.findByFile(fileMetadata).getEncryptionKey());
        Resource resource = FileUtils.download(fileMetadata.getUniqueFileName(), username, key);
        saveFileAction(fileMetadata, userService.findByUsername(username), Action.DOWNLOAD, "Скачен файл");
        return resource;
    }

    public void delete(String filename, String username) {
        FileMetadata fileMetadata = getFileMetadata(filename);
        MyUser user = userService.findByUsername(username);
        if (!user.equals(fileMetadata.getUser())) {
            String message = String.format("Попытка удалить не собственный файл %s пользователем %s", filename, username);
            saveFileAction(fileMetadata, user, Action.ERROR_DELETE, message);
            throw new RuntimeException(message);
        }
        if (fileMetadata.getDeleted()) {
            String message = String.format("Файл %s уже был удален!", filename);
            saveFileAction(fileMetadata, user, Action.ERROR_DELETE, message);
            throw new RuntimeException(message);
        }
        FileUtils.delete(fileMetadata.getUniqueFileName(), username);
        fileMetadata.setDeleted(true);
        saveFileAction(fileMetadata, user, Action.DELETE, "Удален файл");
    }

    private FileMetadata getFileMetadata(String filename) {
        return fileMetadataRepository.findByUniqueFileName(filename)
                .orElseThrow(() -> new FileProcessingException("Файл с именем " + filename + " не найден"));
    }

    private FileMetadata saveFileMetadata(MultipartFile file, MyUser user, String uniqueFileName) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setUser(user);
        fileMetadata.setOriginFileName(file.getOriginalFilename());
        fileMetadata.setUniqueFileName(uniqueFileName);
        fileMetadata.setFileSize(file.getSize());
        fileMetadata.setFileType(file.getContentType());
        fileMetadata.setCategory(getCategory(file.getOriginalFilename()).getIntValue());
        fileMetadata.setDeleted(false);
        fileMetadataRepository.save(fileMetadata);
        saveFileAction(fileMetadata, user, Action.SAVE, "Сохранен файл");
        return fileMetadata;
    }

    private void uploadFile(MultipartFile file, MyUser user) {
        String uuid = getUuid();
        String uniqueFileName = file.getOriginalFilename() + "_" + uuid;
        FileMetadata fileMetadata = saveFileMetadata(file, user, uniqueFileName);
        SecretKey key = generateKey();
        FileUtils.upload(file, user.getUsername(), key, uniqueFileName);
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
