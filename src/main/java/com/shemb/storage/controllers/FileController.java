package com.shemb.storage.controllers;

import com.shemb.storage.converters.FileMetadataConverter;
import com.shemb.storage.dtos.dto.FileMetadataDto;
import com.shemb.storage.dtos.dto.StorageInfo;
import com.shemb.storage.exceptions.FileProcessingException;
import com.shemb.storage.services.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static com.shemb.storage.utils.Utils.createResponseMsg;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {
    private final StorageService storageService;
    private final FileMetadataConverter fileMetadataConverter;

    @GetMapping("/documents")
    public ResponseEntity<List<FileMetadataDto>> getDocumentsList() {
        List<FileMetadataDto> list = storageService.getAllFilesAndAllowed("Nik").stream()
                .map(fileMetadataConverter::entityToDto)
                .filter(file -> !file.getCategory().equals("MEDIA"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/media")
    public ResponseEntity<List<FileMetadataDto>> getMediaList() {
        List<FileMetadataDto> list = storageService.getAllFilesAndAllowed("Nik").stream()
                .map(fileMetadataConverter::entityToDto)
                .filter(file -> file.getCategory().equals("MEDIA"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("files") MultipartFile[] files) {
        List<String> notAllowedFiles = storageService.uploadFile(files, "Nik");
        if (notAllowedFiles.isEmpty()) {
            return ResponseEntity.ok("Файлы успешно загружены");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createResponseMsg("Файлы заражены вирусом и запрещены для хранения: ", notAllowedFiles));
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            Resource resource = storageService.download(filename, "Nik");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                    .body(resource);
        } catch (FileProcessingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try {
            storageService.delete(filename, "Nik");
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/storage-info")
    public StorageInfo getStorageInfo() {
        return storageService.getStorageInfo();
    }
}
