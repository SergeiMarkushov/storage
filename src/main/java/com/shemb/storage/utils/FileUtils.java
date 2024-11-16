package com.shemb.storage.utils;

import com.shemb.storage.dtos.dto.StorageInfo;
import com.shemb.storage.dtos.enums.FileCategory;
import com.shemb.storage.exceptions.FileProcessingException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//@UtilityClass
public class FileUtils {
    private static final ConcurrentMap<String, Path> tempPaths = new ConcurrentHashMap<>();

    private static Path createDir(String username) {
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

    public static void upload(MultipartFile file, String username, SecretKey key, String uniqueFileName) {
        try {
            Path path = createDir(username).resolve(Objects.requireNonNull(uniqueFileName));
            encryptFile(file.getInputStream(), path, key);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new FileProcessingException("Ошибка сохранения файла");
        }
    }

    public static Resource download(String filename, String username, SecretKey key) {
        try {
            Path userDir = createDir(username);
            Path filePath = userDir.resolve(Objects.requireNonNull(filename));
            Path tempFilePath = userDir.resolve("temp-" + Objects.requireNonNull(filename));
            addTempPath(filename, tempFilePath);
            decryptFile(filePath, tempFilePath, key);
            return new UrlResource(tempFilePath.toUri());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new FileProcessingException("Ошибка скачивания файла");
        }
    }

    public static void delete(String filename, String username) {
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

    public static StorageInfo getStorageInfo() {
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

    private static void encryptFile(InputStream inputFile, Path outputFile, SecretKey key) throws
            NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        try (FileOutputStream fos = new FileOutputStream(outputFile.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputFile.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void decryptFile(Path inputFile, Path outputFile, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        try (FileInputStream fis = new FileInputStream(inputFile.toFile());
             FileOutputStream fos = new FileOutputStream(outputFile.toFile());
             CipherInputStream cis = new CipherInputStream(fis, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void addTempPath(String key, Path path) {
        tempPaths.put(key, path);
    }

    public static void deleteTempPaths() {
        for (Path tempPath : tempPaths.values()) {
            if (tempPath != null) {
                try {
                    Files.delete(tempPath);
                } catch (IOException e) {
                    throw new FileProcessingException("Ошибка удаления копии файла " + tempPath);
                }
            }
        }
        tempPaths.clear();
    }

    /**
     * Метод больше для того, чтобы отделить медиа файлы от остальных.
     * Определение категории файла в зависимости от его расширения
     * Предполагается расширять список расширений
     *
     * @param fileName имя файла
     * @return возвращает enum FileCategory
     */
    public static FileCategory getCategory(String fileName) {
        String ext = getFileExt(fileName);
        return switch (ext) {
            case "doc", "docx", "txt", "pdf", "html", "xlsx", "zip", "rar" -> FileCategory.DOCUMENT;
            case "jpg", "jpeg", "png", "mp3", "mp4", "webm" -> FileCategory.MEDIA;
            default -> FileCategory.UNDEFINED;
        };
    }

    private static String getFileExt(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(index + 1).toLowerCase();
        } else {
            throw new IllegalArgumentException("Файл без расширения: " + fileName);
        }
    }

    /**
     * Метод получает расширение файла и проверяет есть ли в списке указанных
     * расширений, которые необходимо просканировать
     *
     * @param fileName имя файла
     * @return возвращает true для потенциально опасных файлов
     */
    public static boolean isScanFile(String fileName) {
        String ext = getFileExt(fileName);
        return switch (ext) {
            case "exe", "com", "bat", "cmd", "scr", "pif", "gadget", "msi", "msp", "sh", "js", "vbs", "wsf",
                    "ps1", "java", "class", "php", "py", "pl", "rb", "docm", "dotm", "xlsm", "xltm", "pptm",
                    "potm", "rar", "zip", "7z", "iso", "inf", "hta", "html" -> true;
            default -> false;
        };
    }

    public static Path createQuarantineDir(String username) throws IOException {
        Path quarantineDir = Paths.get("quarantine", username);
        if (Files.notExists(quarantineDir)) {
            Files.createDirectories(quarantineDir);
        }
        return quarantineDir;
    }

    public static void deleteFiles(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }
}
