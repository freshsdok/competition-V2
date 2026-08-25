package com.teaching.common.core.utils.poi;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.file.Files;

public class InMemoryMultipartFile implements MultipartFile {

    private final byte[] fileContent;
    private final String fileName;
    private final String contentType;

    public InMemoryMultipartFile(byte[] fileContent, String fileName, String contentType) {
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent == null || fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.write(dest.toPath(), fileContent);
    }
}