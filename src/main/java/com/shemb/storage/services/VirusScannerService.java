package com.shemb.storage.services;

import com.shemb.storage.exceptions.FileProcessingException;
import io.sensesecure.clamav4j.ClamAV;
import io.sensesecure.clamav4j.ClamAVException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.shemb.storage.dtos.constants.Const.QUARANTINE;
import static com.shemb.storage.utils.FileUtils.createDir;
import static com.shemb.storage.utils.FileUtils.deleteFiles;

@Service
@RequiredArgsConstructor
public class VirusScannerService {

    private final int CHUNK_SIZE = 1000 * 1024 * 1024;
    private final ClamAV clamavClient;

    public boolean isFileInfected(MultipartFile file, String username) {
        Path quarantineDir = null;
        try {
            quarantineDir = createDir(username, QUARANTINE);
            Path filePath = quarantineDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));

            if (!Files.exists(filePath)) {
                try {
                    Files.write(filePath, file.getBytes());
                } catch (IOException e) {
                    throw new FileProcessingException(e.getMessage());
                }
            }

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new FileProcessingException("Файл не существует или не читается");
            }

            if (file.getSize() > CHUNK_SIZE) {
                return scanFileParts(filePath.toFile());
            }
            return scanFile(filePath.toFile());
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка создания директории карантин");
        } finally {
            assert quarantineDir != null;
            deleteFiles(quarantineDir.toFile());
        }

    }

    private boolean scanFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            try {
                return clamavClient.scan(fis).equals("OK");
            } catch (IOException | ClamAVException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean scanFileParts(File file) {
        boolean isClean = true;
        splitFile(file);
        int partNumber = 0;
        File partFile;
        while ((partFile = new File(file.getParent(), file.getName() + ".part" + partNumber)).exists()) {
            isClean = scanFile(partFile);
            if (!isClean) {
                break;
            }
            partNumber++;
        }
        return isClean;
    }

    private void splitFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int partNumber = 0;

            while ((bytesRead = fis.read(buffer)) > 0) {
                File newFile = new File(file.getParent(), file.getName() + ".part" + partNumber++);
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
