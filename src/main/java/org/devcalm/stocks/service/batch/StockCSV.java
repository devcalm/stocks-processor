package org.devcalm.stocks.service.batch;

public record StockCSV(String date, String open, String high, String low, String close, String adjClose, String volume) {
}
