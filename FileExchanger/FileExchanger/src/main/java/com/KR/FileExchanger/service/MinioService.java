package com.KR.FileExchanger.service;

import com.KR.FileExchanger.exception.FileStorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MinioService implements FileStorageService {

    private final MinioClient minioClient;

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
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

        return fileNames;
    }

    @Override
    public void uploadFile(MultipartFile file) throws Exception {
        ensureBucketExists();

        logger.info("Uploading file: {}", file.getOriginalFilename());

        try {
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
            throw e;
        }
    }

    @Override
    public InputStream downloadFile(String filename) throws Exception {
        ensureBucketExists();
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        );
    }

    @Override
    public void deleteFile(String filename) throws Exception {
        ensureBucketExists();

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        );
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
