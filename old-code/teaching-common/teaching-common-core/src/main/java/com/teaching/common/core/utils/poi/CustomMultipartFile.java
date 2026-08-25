package com.teaching.common.core.utils.poi;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CustomMultipartFile implements MultipartFile {

    private final File file;
    private final String contentType;

    public CustomMultipartFile(File file, String contentType) {
        this.file = file;
        this.contentType = contentType;
    }

    public CustomMultipartFile(File file) throws IOException {
        this.file = file;
        this.contentType = Files.probeContentType(file.toPath());
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return file.getName();
    }

    @Override
    public String getContentType() {
        return contentType != null ? contentType : "application/octet-stream";
    }

    @Override
    public boolean isEmpty() {
        return file.length() == 0;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
    }


}


