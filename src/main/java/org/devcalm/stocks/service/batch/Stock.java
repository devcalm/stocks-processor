package org.devcalm.stocks.service.batch;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Stock(String name, LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal adjClose, long volume) {
}
