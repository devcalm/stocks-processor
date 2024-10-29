package org.devcalm.stocks;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class JobTestFileHandler {

    public void copyZipFileToStorage(Path target) {
        try (var stream = getClass().getResourceAsStream("/static/AAPL_2015-08-01_2024-08-31.zip")) {
            Files.copy(Objects.requireNonNull(stream), target);
            assertThat(Files.exists(target)).isTrue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy zip file", e);
        }
    }

    public void deleteFiles(Path... paths) {
        Stream.of(paths).filter(Files::exists).forEach(f -> {
            try {
                Files.delete(f);
                assertThat(Files.exists(f)).isFalse();
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file", e);
            }
        });
    }
}
