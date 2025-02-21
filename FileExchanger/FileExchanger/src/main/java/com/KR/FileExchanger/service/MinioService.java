package com.KR.FileExchanger.service;

import com.KR.FileExchanger.exception.FileDeleteException;
import com.KR.FileExchanger.exception.FileNotFoundException;
import com.KR.FileExchanger.exception.FileUploadException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService implements FileStorageService {

    private final MinioClient minioClient;

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    private final String bucketName;

    public MinioService(MinioClient minioClient, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public List<String> getAllFiles() throws Exception {
        List<String> fileNames = new ArrayList<>();
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build()
        );

        for (Result<Item> result : objects) {
            Item item = result.get();
            fileNames.add(item.objectName()); // Имя файла
        }

        logger.info("Files in bucket '{}': {}", bucketName, fileNames);
        return fileNames;
    }

    @Override
    public void uploadFile(MultipartFile file) throws FileUploadException {
        if (file == null || file.getOriginalFilename().isEmpty()) {
            throw new FileUploadException("File name is empty or invalid", new IllegalArgumentException());
        }
        try {
            ensureBucketExists();
            logger.debug("Uploading file: {}", file.getOriginalFilename());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getOriginalFilename())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            logger.info("File uploaded successfully: {}", file.getOriginalFilename());
        } catch (Exception e) {
            logger.error("Error uploading file: {}", file.getOriginalFilename(), e);
            throw new FileUploadException("Error uploading file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public InputStream downloadFile(String filename) throws FileNotFoundException {
        if (filename == null || filename.isEmpty()) {
            throw new FileNotFoundException("File name is empty or invalid");
        }
        try {
            ensureBucketExists();
            logger.debug("Downloading file...");
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build());
        } catch (Exception e) {
            logger.error("File not found");
            throw new FileNotFoundException("File not found: " + filename);
        }
    }

    @Override
    public void deleteFile(String filename) throws FileDeleteException {
        try {
            ensureBucketExists();
            logger.debug("Deleting file...");
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Error deleting file");
            throw new FileDeleteException("Error deleting file: " + filename);
        }
    }

    public void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        }
    }
}
