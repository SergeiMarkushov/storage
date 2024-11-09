package com.shemb.storage.converters;

import com.shemb.storage.dtos.dto.FileMetadataDto;
import com.shemb.storage.entities.FileMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataConverter {
    public FileMetadataDto entityToDto(FileMetadata fileMetadata) {
        FileMetadataDto dto = new FileMetadataDto();
        dto.setId(fileMetadata.getId());
        dto.setOriginFileName(fileMetadata.getOriginFileName());
        dto.setUniqueFileName(fileMetadata.getUniqueFileName());
        dto.setFileSize(fileMetadata.getFileSize());
        dto.setFileType(fileMetadata.getFileType());
        dto.setUploadDate(fileMetadata.getUploadDate());
        return dto;
    }
}
