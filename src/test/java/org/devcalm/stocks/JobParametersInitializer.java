package org.devcalm.stocks;

import org.devcalm.stocks.configuration.properties.StorageProperties;
import org.instancio.Instancio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class JobParametersInitializer {

    @Autowired
    private StorageProperties storageProperties;

    public Parameters initializeRandom() {
        return new Parameters(
                Instancio.of(String.class).create(),
                Path.of(storageProperties.getLocal(), generateFileName("zip")),
                Path.of(storageProperties.getLocal(), generateFileName("csv")),
                randomString() + randomString(),
                Instancio.of(LocalDate.class).create(),
                Instancio.of(LocalDate.class).create(),
                UUID.randomUUID()
        );
    }

    public Parameters initializeStatic() {
        return new Parameters(
                "test-id",
                Path.of(storageProperties.getLocal(), "AAPL.zip"),
                Path.of(storageProperties.getLocal(), "AAPL.csv"),
                "AAPL",
                LocalDate.of(2021, 1, 1),
                LocalDate.of(2024, 12, 31),
                UUID.fromString("1de4a587-c68a-498f-8f80-39fe6f8d7092")
        );
    }

    private String generateFileName(String extension) {
        return String.format("%s.%s", randomString(), extension);
    }

    private String randomString() {
        return Instancio.of(String.class).create();
    }

    public record Parameters(String objectId, Path compressedFile, Path uncompressedFile, String stockName,
                      LocalDate startDate, LocalDate endDate, UUID traceId) {
    }
}
