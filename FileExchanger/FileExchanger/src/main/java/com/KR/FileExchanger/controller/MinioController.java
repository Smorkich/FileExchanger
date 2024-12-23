package com.KR.FileExchanger.controller;

import com.KR.FileExchanger.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("api/v1/files")
public class MinioController {

    private static final Logger log = LoggerFactory.getLogger(MinioController.class);
    private final FileStorageService fileStorageService;

    public MinioController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        try {
            List<String> files = fileStorageService.getAllFiles(); // Получение списка файлов через сервис
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            fileStorageService.uploadFile(file);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

//    @GetMapping("/download/{filename}")
//    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) throws Exception {
//        log.info("Downloading file: {}", filename);
//
//        try (InputStream inputStream = fileStorageService.downloadFile(filename)) {
//            byte[] content = inputStream.readAllBytes();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentDisposition(ContentDisposition.builder("attachment")
//                    .filename(filename, StandardCharsets.UTF_8)
//                    .build());
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//
//            log.info("Successfully downloaded file: {}", filename);
//            return new ResponseEntity<>(content, headers, HttpStatus.OK);
//        }
//    }

    @GetMapping("/download/{filename}")
    public void downloadFile(@PathVariable String filename, HttpServletResponse response) throws Exception {
        log.info("Downloading file: {}", filename);

        try (InputStream inputStream = fileStorageService.downloadFile(filename);
             OutputStream outputStream = response.getOutputStream()) {

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString());

            inputStream.transferTo(outputStream);
            log.info("Successfully downloaded file: {}", filename);
        }
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        try {
            fileStorageService.deleteFile(filename);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting file: " + e.getMessage());
        }
    }
}
