package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.exception.StockException;

import java.util.Optional;
import java.util.regex.Pattern;

public class FileNameParser {

    private final Optional<String> name;
    private final Optional<String> startDate;
    private final Optional<String> endDate;

    /**
     * Remote file name should match as following example: AAPL_2015-08-01_2024-08-31.zip
     **/
    private static final Pattern FULL_COMPLIANCE = Pattern.compile("^(?<name>[A-Za-z0-9.]+)_(?<start>\\d{4}-\\d{2}-\\d{2})_(?<end>\\d{4}-\\d{2}-\\d{2})\\.zip");

    public FileNameParser(String filename) {
        var mather = FULL_COMPLIANCE.matcher(filename);

        if (mather.matches()) {
            name = Optional.of(mather.group("name"));
            startDate = Optional.of(mather.group("start"));
            endDate = Optional.of(mather.group("end"));
        } else {
            name = startDate = endDate = Optional.empty();
        }
    }

    public String getNameOrThrowException() {
        return name.orElseThrow(() -> new StockException("Stock name is not set"));
    }

    public String getStartDateOrThrowException() {
        return startDate.orElseThrow(() -> new StockException("Stock start date is not set"));
    }

    public String getEndDateOrThrowException() {
        return endDate.orElseThrow(() -> new StockException("Stock end date is not set"));
    }
}
