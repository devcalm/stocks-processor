package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.configuration.properties.StorageProperties;
import org.devcalm.stocks.exception.StockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@SpringJUnitConfig(UnzipFileServiceTest.Config.class)
@TestPropertySource(properties = {
        "storage.local=storage"
})
class UnzipFileServiceTest {

    @Autowired
    private StorageProperties properties;
    private final UnzipFileService service = new UnzipFileService();

    @Test
    void shouldThrowExceptionWhenZipIsNull() {
        String notZipFile = getFilePathFromResource("/application.yml");

        assertThatThrownBy(() -> service.unzipFile(notZipFile, ""))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Zip entry is null");
    }

    @Test
    void shouldThrowExceptionWhenZipContainsDirectory() {
        String zipWithDirectory = getFilePathFromResource("/static/compressed_directory.zip");

        assertThatThrownBy(() -> service.unzipFile(zipWithDirectory, ""))
                .isInstanceOf(StockException.class)
                .hasMessageContaining("Directory is not allowed");
    }

    @Test
    void shouldSaveUnzippedFile() throws IOException {
        var fileName = "test_AAPL_2015-08-01_2024-08-31.csv";
        var filePath = Path.of(properties.getLocal(), fileName);
        var zippedFile = getFilePathFromResource("/static/AAPL_2015-08-01_2024-08-31.zip");

        service.unzipFile(zippedFile, filePath.toString());

        assertThat(Files.exists(filePath)).isTrue();
        assertThat(Files.readAllLines(filePath)).isNotEmpty();
        Files.delete(filePath);
        assertThat(Files.exists(filePath)).isFalse();
    }

    private String getFilePathFromResource(String fileName) {
        var resource = Objects.requireNonNull(getClass().getResource(fileName), "Not found resource: " + fileName);
        return resource.getPath();
    }

    @EnableConfigurationProperties(StorageProperties.class)
    static class Config {
    }
}