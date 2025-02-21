package com.KR.FileExchanger;

import com.KR.FileExchanger.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MinioServiceTest {
    @Mock
    private MinioClient minioClient;
    @InjectMocks
    private MinioService minioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        minioService = new MinioService(minioClient, "fileuploads");
    }

    @Test
    void testUploadFile() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));
        when(mockFile.getSize()).thenReturn(11L);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.getContentType()).thenReturn("text/plain");

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        assertDoesNotThrow(() -> minioService.uploadFile(mockFile));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testDownloadFile() throws Exception {
        InputStream mockInputStream = new ByteArrayInputStream("test content".getBytes());

        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.readAllBytes()).thenReturn(mockInputStream.readAllBytes());

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(mockResponse);

        InputStream result = minioService.downloadFile("test.txt");

        assertNotNull(result);
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }

    @Test
    void testDeleteFile() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertDoesNotThrow(() -> minioService.deleteFile("test.txt"));

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void testGetAllFiles() throws Exception {
        Result<Item> mockResult = mock(Result.class);
        Item mockItem = mock(Item.class);
        when(mockItem.objectName()).thenReturn("test.txt");
        when(mockResult.get()).thenReturn(mockItem);

        Iterable<Result<Item>> mockResults = Collections.singletonList(mockResult);
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(mockResults);
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        List<String> fileNames = minioService.getAllFiles();

        assertNotNull(fileNames);
        assertEquals(1, fileNames.size());
        assertEquals("test.txt", fileNames.get(0));

        verify(minioClient, times(1)).listObjects(any(ListObjectsArgs.class));
    }

    //Fails tests________________________________________________________________________________________

    @Test
    void testUploadFileFails() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));
        when(mockFile.getSize()).thenReturn(11L);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.getContentType()).thenReturn("text/plain");

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("Minio is down"));

        Exception ex = assertThrows(RuntimeException.class, () -> minioService.uploadFile(mockFile));

        assertEquals("Minio is down", ex.getMessage());
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void testDownloadFileFails() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("File not found"));

        Exception ex = assertThrows(RuntimeException.class, () -> minioService.downloadFile("file.txt"));

        assertEquals("File not found", ex.getMessage());
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }
    @Test
    void testDeleteFileFails() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        Exception ex = assertThrows(RuntimeException.class, () -> minioService.deleteFile("file.txt"));
        assertEquals("Delete failed", ex.getMessage());
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }
    @Test
    void testGetAllFilesFails() throws Exception {
        when(minioClient.listObjects(any(ListObjectsArgs.class)))
                .thenThrow(new RuntimeException("Minio error"));

        Exception ex = assertThrows(RuntimeException.class, () -> minioService.getAllFiles());

        assertEquals("Minio error", ex.getMessage());
        verify(minioClient, times(1)).listObjects(any(ListObjectsArgs.class));
    }
}
