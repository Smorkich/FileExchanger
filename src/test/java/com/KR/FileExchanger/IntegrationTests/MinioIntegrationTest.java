package com.KR.FileExchanger.IntegrationTests;

import io.minio.MinioClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MinioIntegrationTest {
    private static final String MINIO_IMAGE = "minio/minio:latest";
    private static final String ACCESS_KEY = "admin";
    private static final String SECRET_KEY = "password";
    private static final GenericContainer<?> minioContainer = new GenericContainer<>(DockerImageName.parse(MINIO_IMAGE))
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server /data")
            .withExposedPorts(9000);

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
        System.setProperty("MINIO_URL", "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        System.setProperty("MINIO_ACCESS_KEY", ACCESS_KEY);
        System.setProperty("MINIO_SECRET_KEY", SECRET_KEY);
    }
    @Autowired
    MinioClient minioClient;

    @Test
    void testMinioConnection() {
        Assertions.assertNotNull(minioClient);
    }

}
