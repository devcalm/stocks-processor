package org.devcalm.stocks;

import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.io.OutputStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.devcalm.stocks.ConstanceHolder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBatchTest
@SpringBootTest
class StockBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JobParametersInitializer jobParametersInitializer;
    @Autowired
    private JobTestFileHandler jobTestFileHandler;
    @MockBean
    private FirebaseApp firebaseApp;
    @MockBean
    private PubSubInboundChannelAdapter adapter;
    @MockBean
    private Bucket bucket;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void shouldCompleteStockJob(JobParametersInitializer.Parameters parameters, int countRows) throws Exception {
        // given
        jobTestFileHandler.copyZipFileToStorage(parameters.compressedFile());

        var jobParameters = new JobParametersBuilder()
                .addString(BATCH_REMOTE_COMPRESSED_FILE, parameters.objectId())
                .addString(BATCH_LOCAL_COMPRESSED_FILE, parameters.compressedFile().toString())
                .addString(BATCH_LOCAL_UNCOMPRESSED_FILE, parameters.uncompressedFile().toString())
                .addString(BATCH_STOCK_NAME, parameters.stockName())
                .addString(BATCH_STOCK_START, parameters.startDate().toString())
                .addString(BATCH_STOCK_END, parameters.endDate().toString())
                .addString(BATCH_TRACE_ID, parameters.traceId().toString())
                .toJobParameters();

        var blob = mock(Blob.class);
        given(bucket.get(any(String.class)))
                .willReturn(blob);

        willDoNothing().given(blob)
                .downloadTo(any(OutputStream.class));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "stocks")).isEqualTo(countRows);

        jobTestFileHandler.deleteFiles(parameters.compressedFile(), parameters.uncompressedFile());
    }

    private Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(jobParametersInitializer.initializeRandom(), 5),
                Arguments.of(jobParametersInitializer.initializeStatic(), 10),
                Arguments.of(jobParametersInitializer.initializeStatic(), 10)
        );
    }
}
