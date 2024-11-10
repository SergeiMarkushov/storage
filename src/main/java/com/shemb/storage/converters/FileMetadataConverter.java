package com.shemb.storage.converters;

import com.shemb.storage.dtos.dto.FileMetadataDto;
import com.shemb.storage.entities.FileMetadata;
import org.springframework.stereotype.Component;

import static com.shemb.storage.dtos.enums.FileCategory.byIntValue;

@Component
public class FileMetadataConverter {
    public FileMetadataDto entityToDto(FileMetadata fileMetadata) {
        FileMetadataDto dto = new FileMetadataDto();
        dto.setId(fileMetadata.getId());
        dto.setOriginFileName(fileMetadata.getOriginFileName());
        dto.setUniqueFileName(fileMetadata.getUniqueFileName());
        dto.setFileSize(fileMetadata.getFileSize());
        dto.setFileType(fileMetadata.getFileType());
        dto.setCategory(byIntValue(fileMetadata.getCategory()).toString());
        dto.setUploadDate(fileMetadata.getUploadDate());
        return dto;
    }
}
