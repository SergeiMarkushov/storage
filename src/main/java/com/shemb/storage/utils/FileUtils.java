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
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.shemb.storage.dtos.constants.Const.QUARANTINE;
import static com.shemb.storage.dtos.constants.Const.UPLOADS;

//@UtilityClass
public class FileUtils {
    private static final ConcurrentMap<LocalDateTime, Path> tempPaths = new ConcurrentHashMap<>();
    private static final Path uploadsDir = Paths.get(System.getProperty("user.home"), UPLOADS);
    private static final Path quarantineDir = Paths.get(QUARANTINE);

    /**
     * Метод создает папку в корне проекта или в домашней директории.
     * Поддержаны папки для хранения файлов пользователя и карантин
     *
     * @param username Имя пользователя
     * @param folder   название папки
     * @return возвращает созданную директорию
     */
    public static Path createDir(String username, String folder) throws IOException {
        Path directory = null;
        switch (folder) {
            case QUARANTINE -> directory = quarantineDir.resolve(username);
            case UPLOADS -> directory = uploadsDir.resolve(username);
        }
        if (Files.notExists(directory)) {
            Files.createDirectories(directory);
        }
        return directory;
    }

    public static void upload(MultipartFile file, String username, SecretKey key, String uniqueFileName) {
        try {
            Path path = createDir(username, UPLOADS).resolve(Objects.requireNonNull(uniqueFileName));
            encryptFile(file.getInputStream(), path, key);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new FileProcessingException("Ошибка сохранения файла");
        }
    }

    public static Resource download(String filename, String username, SecretKey key) {
        try {
            Path userDir = createDir(username, UPLOADS);
            Path filePath = userDir.resolve(Objects.requireNonNull(filename));
            Path tempFilePath = userDir.resolve("temp-" + Objects.requireNonNull(filename));
            addTempPath(LocalDateTime.now(), tempFilePath);
            decryptFile(filePath, tempFilePath, key);
            return new UrlResource(tempFilePath.toUri());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new FileProcessingException("Ошибка скачивания файла");
        }
    }

    private static void delete(Path path) {
        try {
            delete(null, null, path);
        } catch (IOException e) {
            throw new FileProcessingException("Файл " + path.toString() + " не найден");
        }
    }

    public static void delete(String filename, String username) {
        try {
            delete(filename, username, null);
        } catch (IOException e) {
            throw new FileProcessingException("Файл " + filename + " не найден");
        }
    }

    private static void delete(String filename, String username, Path path) throws IOException {
        Path deletePath;
        if (path != null) {
            deletePath = path;
        } else {
            deletePath = createDir(username, UPLOADS).resolve(Objects.requireNonNull(filename));
        }
        if (Files.exists(deletePath)) {
            Files.delete(deletePath);
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

    private static void addTempPath(LocalDateTime key, Path path) {
        if (!tempPaths.containsValue(path)) {
            tempPaths.put(key, path);
        }
    }

    public static void deleteTempPaths() {
        long time = 5;
        for (Map.Entry<LocalDateTime, Path> entry : tempPaths.entrySet()) {
            if (entry.getKey().plusMinutes(time).isBefore(LocalDateTime.now())) {
                System.out.println("key: " + entry.getKey() + "  |  value " + entry.getValue());
                delete(entry.getValue());
                tempPaths.remove(entry.getKey());
            }
        }
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

    /**
     * Метод для удаления всех файлов в переданной директории
     *
     * @param directory директория, в которой нужно удалить все файлы
     */
    public static void deleteFiles(File directory) {
        if (directory.isDirectory()) {
            deleteFiles(directory.listFiles());
        }
    }

    private static void deleteFiles(File[] files) {
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    private static List<Path> findTempFiles() {
        Queue<File> folders = new LinkedList<>();
        folders.add(new File(String.valueOf(uploadsDir)));
        List<Path> tempFiles = null;
        while (!folders.isEmpty()) {
            File folder = folders.poll();
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("temp_")) {
                        tempFiles.add(file.toPath());
                    }
                }
            }
        }
        return tempFiles;
    }

    /**
     * Метод для того, чтобы удалять файлы, которые отсутствуют с мапе на удаление.
     * Этот метод будет дергать шедуллер, по расписанию. Метод получает список временных
     * файлов из папки uploads, затем проверяет, что этих файлов нет в мапе на удаление
     * и удаляет те, которых нет.
     */
    public static void deleteTempFiles() {
        for (Path tempPath : Objects.requireNonNull(findTempFiles())) {
            if (!tempPaths.containsValue(tempPath)) {
                delete(tempPath);
            }
        }
    }
}
