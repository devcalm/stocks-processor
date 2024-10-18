package org.devcalm.stocks.service.storage;

import java.time.LocalDate;

public record StockMetadata(String name, String uncompressedFile, String compressedFile, LocalDate start, LocalDate end) {
}
