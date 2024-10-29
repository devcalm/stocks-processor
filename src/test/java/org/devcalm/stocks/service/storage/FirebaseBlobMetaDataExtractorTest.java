package org.devcalm.stocks.service.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.devcalm.stocks.exception.StockException;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.devcalm.stocks.ConstanceHolder.*;
import static org.mockito.BDDMockito.*;

class FirebaseBlobMetaDataExtractorTest {

    @Mock
    private Bucket bucket;

    @InjectMocks
    private FirebaseBlobMetaDataExtractor extractor;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void shouldExtractAllValuesFromMetaData() {
        var filename = generateFileName();
        var remoteFileName = generateRemoteFileName(filename);
        var blob = generateBlob();

        var metadata = new HashMap<String, String>() {{
            put(BATCH_STOCK_NAME, "AAPL");
            put(BATCH_STOCK_START, "2015-08-01");
            put(BATCH_STOCK_END, "2024-08-31");
            put(BATCH_REMOTE_FILE_NAME, remoteFileName);
        }};

        given(blob.getMetadata()).willReturn(metadata);
        given(bucket.get(remoteFileName)).willReturn(blob);

        var result = extractor.extract(remoteFileName);
        var expectedResult = new StockMetadata("AAPL", remoteFileName, filename,
                LocalDate.parse("2015-08-01"), LocalDate.parse("2024-08-31"));

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldExtractAllDefaultValues() {
        var filename = "MSFT_2020-08-01_2024-09-30.zip";
        var remoteFileName = generateRemoteFileName(filename);
        var blob = generateBlob();

        given(blob.getMetadata()).willReturn(new HashMap<>());
        given(bucket.get(remoteFileName)).willReturn(blob);

        var result = extractor.extract(remoteFileName);
        var expectedResult = new StockMetadata("MSFT", "MSFT_2020-08-01_2024-09-30.csv", filename,
                LocalDate.parse("2020-08-01"), LocalDate.parse("2024-09-30"));

        assertThat(result).isEqualTo(expectedResult);
    }

    private Blob generateBlob() {
        var blob = mock(Blob.class);
        given(blob.getContentType()).willReturn("application/zip");
        return blob;
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist() {
        var filename = generateFileName();
        given(bucket.get(filename)).willReturn(null);

        assertThatThrownBy(() -> extractor.extract(filename))
                .isInstanceOf(StockException.class)
                .hasMessageContaining("File %s not found in the bucket".formatted(filename));
    }

    @ParameterizedTest
    @MethodSource("notCompressedFile")
    void shouldThrowExceptionWhenFileIsNotCompressed(Blob blob) {
        var filename = generateFileName();
        given(bucket.get(filename)).willReturn(blob);

        assertThatThrownBy(() -> extractor.extract(filename))
                .isInstanceOf(StockException.class)
                .hasMessageContaining("File %s must be compressed".formatted(filename));
    }

    private static Stream<Arguments> notCompressedFile() {
        var emptyContentType = mock(Blob.class);
        var htmlContentType = mock(Blob.class);

        given(emptyContentType.getContentType()).willReturn(null);
        given(htmlContentType.getContentType()).willReturn("text/html");

        return Stream.of(Arguments.of(emptyContentType), Arguments.of(htmlContentType));
    }

    private String generateRemoteFileName(String filename) {
        var folder = Instancio.of(String.class).create().toLowerCase();
        return folder + "/" + filename;
    }

    private String generateFileName() {
        return Instancio.of(String.class).create().toLowerCase() + ".zip";
    }
}