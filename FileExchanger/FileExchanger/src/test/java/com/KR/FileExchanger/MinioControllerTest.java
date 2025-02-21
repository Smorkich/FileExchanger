package com.KR.FileExchanger;

import com.KR.FileExchanger.controller.MinioController;
import com.KR.FileExchanger.service.FileStorageService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class MinioControllerTest {
    @Mock
    FileStorageService fileStorageService;
    @Mock
    HttpServletResponse httpServletResponse;
    @Mock
    private MultipartFile multipartFile; // Добавляем мок файла
    @InjectMocks
    MinioController minioController;

    private ByteArrayOutputStream byteArrayOutputStream;
    @BeforeEach
    void setUp() {
        openMocks(this);
        byteArrayOutputStream = new ByteArrayOutputStream();
    }
    @Test
    void testListFiles() throws Exception {
        List<String> list = Arrays.asList("file1.txt", "file2.txt");
        when(fileStorageService.getAllFiles()).thenReturn(list);
        ResponseEntity<List<String>> responseEntity = minioController.listFiles();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(list, responseEntity.getBody());
        verify(fileStorageService, times(1)).getAllFiles();
    }

    @Test
    void testUploadFile() throws Exception {
        doNothing().when(fileStorageService).uploadFile(multipartFile);
        ResponseEntity<String> responseEntity = minioController.uploadFile(multipartFile);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("File uploaded successfully", responseEntity.getBody());
        verify(fileStorageService, times(1)).uploadFile(multipartFile);
    }
    @Test
    void testDownloadFile() throws Exception {
        InputStream mockIS = new ByteArrayInputStream("test content".getBytes());
        // Создаём мок ServletOutputStream
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) {
                byteArrayOutputStream.write(b);
            }
        };

        when(fileStorageService.downloadFile("test.txt")).thenReturn(mockIS);
        when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        minioController.downloadFile("test.txt", httpServletResponse);

        verify(fileStorageService, times(1)).downloadFile("test.txt");
        verify(httpServletResponse, times(1)).setContentType(anyString());
        verify(httpServletResponse, times(1)).setHeader(anyString(), anyString());


        assertEquals("test content", byteArrayOutputStream.toString());
    }

    @Test
    void testDeleteFile() throws Exception {
        doNothing().when(fileStorageService).deleteFile("test.txt");
        ResponseEntity<String> responseEntity = minioController.deleteFile("test.txt");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("File deleted successfully", responseEntity.getBody());
        verify(fileStorageService, times(1)).deleteFile("test.txt");
    }
    //Fails tests________________________________________________________________________________________

    @Test
    void testUploadFileFails() throws Exception {
        doThrow(new Exception("Upload failed")).when(fileStorageService).uploadFile(any(MultipartFile.class));
        ResponseEntity<String> responseEntity = minioController.uploadFile(multipartFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Error uploading file: Upload failed", responseEntity.getBody());
    }

    @Test
    void testDownloadFileFails() throws Exception {
        when(fileStorageService.downloadFile(anyString()))
                .thenThrow(new RuntimeException("File not found"));
        Exception ex = assertThrows(RuntimeException.class,
                () -> minioController.downloadFile("file", httpServletResponse));

        assertEquals("File not found", ex.getMessage());
        verify(fileStorageService, times(1)).downloadFile(anyString());
    }

    @Test
    void testDeleteFailFails() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(fileStorageService).deleteFile(anyString());

        ResponseEntity<String> responseEntity = minioController.deleteFile("file");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Error deleting file: Delete failed", responseEntity.getBody());
        verify(fileStorageService, times(1)).deleteFile(anyString());
    }

    @Test
    void testListFilesFails() throws Exception {
        when(fileStorageService.getAllFiles()).thenThrow(new RuntimeException("MinIO error"));

        ResponseEntity<List<String>> response = minioController.listFiles();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}
