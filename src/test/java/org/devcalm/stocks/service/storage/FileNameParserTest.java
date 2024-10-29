package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.exception.StockException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileNameParserTest {

    @Test
    void shouldParseCorrectFileName() {
        var fileNameParse = new FileNameParser("MSFT_2020-08-01_2024-09-30.zip");

        assertThat(fileNameParse.getNameOrThrowException()).isEqualTo("MSFT");
        assertThat(fileNameParse.getStartDateOrThrowException()).isEqualTo("2020-08-01");
        assertThat(fileNameParse.getEndDateOrThrowException()).isEqualTo("2024-09-30");
    }

    @Test
    void shouldThrowExceptionWhenFileNameIsNotCorrect() {
        var fileNameParse = new FileNameParser("MSFT_2020-08-01");

        assertThatThrownBy(fileNameParse::getNameOrThrowException)
                .isInstanceOf(StockException.class)
                .hasMessageContaining("Stock name is not set");

        assertThatThrownBy(fileNameParse::getStartDateOrThrowException)
                .isInstanceOf(StockException.class)
                .hasMessageContaining("Stock start date is not set");

        assertThatThrownBy(fileNameParse::getEndDateOrThrowException)
                .isInstanceOf(StockException.class)
                .hasMessageContaining("Stock end date is not set");
    }
}