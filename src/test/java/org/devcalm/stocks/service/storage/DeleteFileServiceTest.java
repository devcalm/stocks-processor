package org.devcalm.stocks.service.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.devcalm.stocks.exception.StockException;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

class DeleteFileServiceTest {

    @Mock
    private Bucket bucket;
    @InjectMocks
    private DeleteFileService deleteFileService;
    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldDeleteLocalFile() throws IOException {
        Path tempFile = Files.createTempFile(generateFileName(), "txt");

        deleteFileService.deleteLocal(tempFile);

        assertThat(Files.exists(tempFile)).isFalse();
    }

    @Test
    void shouldExceptionForDeleteLocalFile() {
        Path tempFile = Path.of("non-existent-file.txt");

        assertThatThrownBy(() -> deleteFileService.deleteLocal(tempFile))
                .isInstanceOf(StockException.class)
                .hasMessageContaining("Cannot delete local uncompressed file: non-existent-file.txt");
    }

    @Test
    void shouldDeleteRemoteFile() {
        String fileName = generateFileName();
        Blob blob = Mockito.mock(Blob.class);

        given(bucket.get(fileName)).willReturn(blob);

        deleteFileService.deleteRemote(fileName);

        then(blob).should().delete();
    }

    private static String generateFileName() {
        return Instancio.of(String.class).create();
    }
}