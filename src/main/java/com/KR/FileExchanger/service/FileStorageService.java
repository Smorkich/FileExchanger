package com.KR.FileExchanger.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface FileStorageService {

    List<String> getAllFiles() throws Exception;

    void uploadFile(MultipartFile file) throws Exception;

    InputStream downloadFile(String filename) throws Exception;

    void deleteFile(String filename) throws Exception;
}
