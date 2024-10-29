package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.exception.StockException;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.devcalm.stocks.ConstanceHolder.*;

@Component
@RequiredArgsConstructor
public class FirebaseBlobMetaDataExtractor {

    private final Bucket bucket;

    public StockMetadata extract(final String filename) {
        Blob blob = bucket.get(filename);
        if (blob == null) {
            throw new StockException("File %s not found in the bucket".formatted(filename));
        }
        if (blob.getContentType() == null || !blob.getContentType().equals("application/zip")) {
            throw new StockException("File %s must be compressed".formatted(filename));
        }
        Map<String, String> metadata = Objects.requireNonNullElse(blob.getMetadata(), new HashMap<>());
        String compressedFile = extractRemoteFile(filename);
        return parse(metadata, compressedFile);
    }

    private StockMetadata parse(Map<String, String> metadata, String compressedFile) {
        var fileNameParser = new FileNameParser(compressedFile);

        var stockName = metadata.computeIfAbsent(BATCH_STOCK_NAME, k -> fileNameParser.getNameOrThrowException());

        var startDate = LocalDate.parse(metadata.computeIfAbsent(BATCH_STOCK_START,
               k -> fileNameParser.getStartDateOrThrowException()), DateTimeFormatter.ISO_DATE);
        var endDate = LocalDate.parse(metadata.computeIfAbsent(BATCH_STOCK_END,
               k -> fileNameParser.getEndDateOrThrowException()), DateTimeFormatter.ISO_DATE);

        var uncompressedFile = metadata.computeIfAbsent(BATCH_REMOTE_FILE_NAME, k-> compressedFile.replaceFirst("zip$", "csv"));

        return new StockMetadata(stockName, uncompressedFile, compressedFile, startDate, endDate);
    }

    private String extractRemoteFile(String filename) {
        return Arrays.stream(filename.split("/"))
                .collect(Collectors.toCollection(LinkedList::new)).getLast();
    }
}
