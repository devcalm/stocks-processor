package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.exception.StockException;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.devcalm.stocks.ConstanceHolder.*;

@Component
@RequiredArgsConstructor
public class FirebaseBlobMetaDataExtractor {

    private static final Pattern regex = Pattern.compile("^(?<name>[A-Za-z0-9\\.]+)_(?<start>\\d{4}-\\d{2}-\\d{2})_(?<end>\\d{4}-\\d{2}-\\d{2})\\.zip");
    private final Bucket bucket;

    public StockMetadata extract(final String filename) {
        Blob blob = bucket.get(filename);
        if (blob == null) {
            throw new StockException("File %s not found in the bucket".formatted(filename));
        }
        if (blob.getContentType() == null || !blob.getContentType().equals("application/zip")) {
            throw new StockException("File %s must be compressed".formatted(filename));
        }
        Map<String, String> metadata = Objects.requireNonNullElse(blob.getMetadata(), Map.of());
        String compressedFile = extractRemoteFile(filename);
        return parse(metadata, compressedFile);
    }

    private StockMetadata parse(Map<String, String> metadata, String compressedFile) {
        Matcher matcher = regex.matcher(compressedFile);

        var stockName = metadata.getOrDefault(BATCH_STOCK_NAME,
                getDefaultValue(matcher, "name", "Stock name is not set").get());

        var startDate = LocalDate.parse(metadata.getOrDefault(BATCH_STOCK_START,
                        getDefaultValue(matcher, "start", "Stock start date is not set").get()),
                DateTimeFormatter.ISO_DATE);

        var endDate = LocalDate.parse(metadata.getOrDefault(BATCH_STOCK_END,
                        getDefaultValue(matcher, "end", "Stock end date is not set").get()),
                DateTimeFormatter.ISO_DATE);

        var uncompressedFile = metadata.getOrDefault(BATCH_REMOTE_FILE_NAME, "stock.csv");

        return new StockMetadata(stockName, uncompressedFile, compressedFile, startDate, endDate);
    }

    private String extractRemoteFile(String filename) {
        return Arrays.stream(filename.split("/"))
                .collect(Collectors.toCollection(LinkedList::new)).getLast();
    }

    private Supplier<String> getDefaultValue(Matcher matcher, String groupName, String errorMessage) {
        return () -> {
            if (matcher.matches()) {
                return matcher.group(groupName);
            }
            throw new StockException(errorMessage);
        };
    }
}
