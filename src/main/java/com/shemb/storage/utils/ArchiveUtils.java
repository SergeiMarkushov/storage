package com.shemb.storage.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ArchiveUtils {
    public static List<String> unpackArchive(String archivePath, String outputDir) throws IOException {
        List<String> fileNames = new ArrayList<>();
        if (archivePath.endsWith(".tar.gz")) {
            try (InputStream fi = Files.newInputStream(Paths.get(archivePath));
                 InputStream bi = new GzipCompressorInputStream(fi);
                 ArchiveInputStream ai = new TarArchiveInputStream(bi)) {

                ArchiveEntry entry;
                while ((entry = ai.getNextEntry()) != null) {
                    if (!ai.canReadEntryData(entry)) {
                        continue;
                    }
                    File file = new File(outputDir, entry.getName());
                    fileNames.add(file.getName());
                    if (entry.isDirectory()) {
                        if (!file.isDirectory() && !file.mkdirs()) {
                            throw new IOException("Failed to create directory " + file);
                        }
                    } else {
                        File parent = file.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        try (FileOutputStream o = new FileOutputStream(file)) {
                            IOUtils.copy(ai, o);
                        }
                    }
                }
            }
        } else if (archivePath.endsWith(".zip")) {
            try (InputStream fi = Files.newInputStream(Paths.get(archivePath));
                 ArchiveInputStream ai = new ZipArchiveInputStream(fi)) {

                ArchiveEntry entry;
                while ((entry = ai.getNextEntry()) != null) {
                    if (!ai.canReadEntryData(entry)) {
                        continue;
                    }
                    File file = new File(outputDir, entry.getName());
                    fileNames.add(file.getName());
                    if (entry.isDirectory()) {
                        if (!file.isDirectory() && !file.mkdirs()) {
                            throw new IOException("Failed to create directory " + file);
                        }
                    } else {
                        File parent = file.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        try (FileOutputStream o = new FileOutputStream(file)) {
                            IOUtils.copy(ai, o);
                        }
                    }
                }
            }
        } else if (archivePath.endsWith(".rar")) {
            try (Archive archive = new Archive(new FileInputStream(archivePath))) {
                FileHeader fileHeader;
                while ((fileHeader = archive.nextFileHeader()) != null) {
                    File file = new File(outputDir, fileHeader.getFileNameString().trim());
                    fileNames.add(file.getName());
                    if (fileHeader.isDirectory()) {
                        if (!file.isDirectory() && !file.mkdirs()) {
                            throw new IOException("Failed to create directory " + file);
                        }
                    } else {
                        File parent = file.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        try (FileOutputStream o = new FileOutputStream(file)) {
                            archive.extractFile(fileHeader, o);
                        }
                    }
                }
            } catch (RarException e) {
                throw new RuntimeException(e);
            }
        }
        return fileNames;
    }
}
