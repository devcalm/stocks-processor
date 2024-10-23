package org.devcalm.stocks.service.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.devcalm.stocks.exception.StockException;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.file.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class FirebaseDownloadFileServiceTest {

    @Mock
    private Bucket bucket;
    @InjectMocks
    private FirebaseDownloadFileService firebaseDownloadFileService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void shouldThrowExceptionWhenBucketIsNull() {
        var filename = generateFileName();
        var destinationPath = mock(Path.class);

        given(bucket.get(filename)).willReturn(null);

        assertThatThrownBy(() -> firebaseDownloadFileService.downloadFile(filename, destinationPath))
                .isInstanceOf(StockException.class)
                .hasMessageContaining("File %s not found in the bucket".formatted(filename));
    }

    @Test
    void shouldDownloadFile() throws Exception {
        var filename = generateFileName();
        var destinationPath = mock(Path.class);
        var blob = Mockito.mock(Blob.class);

        given(bucket.get(filename)).willReturn(blob);

        try (var mockFiles = mockStatic(Files.class)) {
            given(Files.newOutputStream(destinationPath, StandardOpenOption.CREATE))
                    .willReturn(mock(OutputStream.class));

            firebaseDownloadFileService.downloadFile(filename, destinationPath);

            then(blob).should().downloadTo(any(OutputStream.class));
        }
    }

    @Test
    void shouldThrowExceptionWhileDownloadingFile() throws Exception {
        var filename = generateFileName();
        var destinationPath = mock(Path.class);
        var blob = Mockito.mock(Blob.class);

        given(bucket.get(filename)).willReturn(blob);

        try (var mockFiles = mockStatic(Files.class)) {
            given(Files.newOutputStream(destinationPath, StandardOpenOption.CREATE))
                    .willThrow(IOException.class);

            assertThatThrownBy(() -> firebaseDownloadFileService.downloadFile(filename, destinationPath))
                    .isInstanceOf(StockException.class)
                    .hasMessageContaining("Error while downloading compressed file: %s".formatted(filename));
        }
    }

    private String generateFileName() {
        return Instancio.of(String.class).create();
    }
}