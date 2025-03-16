package com.KR.FileExchanger.IntegrationTests;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MinioFileOperationsIT {
    private static final String MINIO_IMAGE = "minio/minio:latest";
    private static final String ACCESS_KEY = "admin";
    private static final String SECRET_KEY = "password";
    private static final String BUCKET_NAME = "fileuploads";

    private static final GenericContainer<?> minioContainer = new GenericContainer<>(DockerImageName.parse(MINIO_IMAGE))
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server /data")
            .withExposedPorts(9000);

    private MinioClient minioClient;
    @BeforeAll
    void setup() throws Exception {
        minioContainer.start();
        String minioUrl = "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000);

        minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();

        // Создаем bucket, если его нет
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
        }
    }
    @Test
    void testUploadFile() throws Exception{
        String fileName = "test.txt";
        String content = "Hello, MinIO!";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(fileName)
                        .stream(inputStream, content.length(), -1)
                        .contentType("text/plain")
                        .build()
        );
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder().bucket(BUCKET_NAME).object(fileName).build()
        );

        assertNotNull(stat);
        assertEquals(fileName, stat.object());
    }
    @Test
    void testDownloadFile() throws Exception {
        String fileName = "download-test.txt";
        String content = "MinIO Download Test";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(fileName)
                        .stream(inputStream, content.length(), -1)
                        .contentType("text/plain")
                        .build()
        );

        // Загружаем файл
        InputStream downloadedStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(BUCKET_NAME).object(fileName).build()
        );

        byte[] buffer = new byte[content.length()];
        assertEquals(downloadedStream.read(buffer), content.length());
        assertEquals(new String(buffer), content);
    }

    @Test
    void testDeleteFile() throws Exception {
        String fileName = "delete-test.txt";
        String content = "File to delete";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(fileName)
                        .stream(inputStream, content.length(), -1)
                        .contentType("text/plain")
                        .build()
        );

        // Удаляем файл
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(fileName).build()
        );

        // Проверяем, что файл больше не существует
        assertThrows(
                ErrorResponseException.class,
                () -> minioClient.statObject(StatObjectArgs.builder().bucket(BUCKET_NAME).object(fileName).build())
        );
    }
}
