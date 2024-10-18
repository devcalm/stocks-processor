package org.devcalm.stocks.configuration;

import org.devcalm.stocks.service.batch.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.devcalm.stocks.ConstanceHolder.BATCH_LOCAL_UNCOMPRESSED_FILE;
import static org.devcalm.stocks.ConstanceHolder.BATCH_STOCK_NAME;

@Configuration
public class StockJobConfiguration {

    @Bean
    public Step step1(JobRepository repository, PlatformTransactionManager transactionManager, DownloadStockTasklet tasklet) {
        return new StepBuilder("DownloadFromStorage", repository).tasklet(tasklet, transactionManager).build();
    }

    @Bean
    public Step step2(JobRepository repository, PlatformTransactionManager transactionManager, UnCompressedStockFileTasklet tasklet) {
        return new StepBuilder("UnCompressFile", repository).tasklet(tasklet, transactionManager).build();
    }

    @Bean
    public Step step3(JobRepository repository, PlatformTransactionManager transactionManager,
                      ItemReader<StockCSV> stockCSVReader,
                      ItemProcessor<StockCSV, Stock> stockItemProcessor,
                      ItemWriter<Stock> stockDataTableWriter) {
        return new StepBuilder("FileIngestion", repository)
                .<StockCSV, Stock>chunk(100, transactionManager)
                .reader(stockCSVReader)
                .processor(stockItemProcessor)
                .writer(stockDataTableWriter)
                .build();
    }

    @Bean
    public Step step4(JobRepository repository, PlatformTransactionManager transactionManager, CleanUpTasklet tasklet) {
        return new StepBuilder("CleanUp", repository).tasklet(tasklet, transactionManager).build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<StockCSV> stockCSVReader(@Value("#{jobParameters}") Map<String, Object> parameters) {
        var file = extractJobParameter(parameters, BATCH_LOCAL_UNCOMPRESSED_FILE, "Uncompressed file is not set");
        return new FlatFileItemReaderBuilder<StockCSV>()
                .name("StockCSVReader")
                .resource(new FileSystemResource(file))
                .linesToSkip(1)
                .delimited()
                .names("date", "open", "high", "low", "close", "adjClose", "volume")
                .targetType(StockCSV.class)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<StockCSV, Stock> stockItemProcessor(@Value("#{jobParameters}") Map<String, Object> parameters) {
        var name = extractJobParameter(parameters, BATCH_STOCK_NAME, "Stock name is not set");
        return csv -> new Stock(name, LocalDate.parse(csv.date(), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                new BigDecimal(csv.open()), new BigDecimal(csv.high()), new BigDecimal(csv.low()),
                new BigDecimal(csv.close()), new BigDecimal(csv.adjClose()), Long.parseLong(csv.volume()));
    }

    @Bean
    public ItemWriter<Stock> stockDataTableWriter(DataSource dataSource, NamedParameterJdbcTemplate namedJdbcTemplate) {
        String sql = """
                INSERT INTO stocks (name, date, open, high, low, close, adj_close, volume)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (name, date) DO NOTHING
                """;
        var writer = new JdbcBatchItemWriterBuilder<Stock>()
                .dataSource(dataSource)
                .sql(sql)
                .itemPreparedStatementSetter((stock, ps) -> {
                    ps.setString(1, stock.name());
                    ps.setObject(2, stock.date());
                    ps.setBigDecimal(3, stock.open());
                    ps.setBigDecimal(4, stock.high());
                    ps.setBigDecimal(5, stock.low());
                    ps.setBigDecimal(6, stock.close());
                    ps.setBigDecimal(7, stock.adjClose());
                    ps.setLong(8, stock.volume());
                })
                .assertUpdates(false)
                .build();
        return items -> {
            var filteredChunks = filterUniqueStocks(items, namedJdbcTemplate);
            writer.write(filteredChunks);
        };
    }

    private Chunk<? extends Stock> filterUniqueStocks(Chunk<? extends Stock> items, NamedParameterJdbcTemplate namedJdbcTemplate) {
        if (items.isEmpty()) {
            return Chunk.of();
        }
        var name = items.getItems().stream().findFirst().map(Stock::name).orElseThrow();
        var dates = items.getItems().stream().map(Stock::date).toList();
        var parameters = new MapSqlParameterSource(Map.of("name", name, "dates", dates));
        var sql = """
                SELECT name, date
                FROM stocks
                WHERE name = :name AND date IN (:dates) 
                """;

        List<StockNameDate> exists = namedJdbcTemplate.query(sql, parameters, (rs, num) ->
                new StockNameDate(rs.getString("name"),
                        LocalDate.parse(rs.getString("date"), DateTimeFormatter.ISO_DATE)));

        return new Chunk<>(items.getItems().stream()
                .filter(i -> !exists.contains(new StockNameDate(i.name(), i.date())))
                .toList());
    }

    @Bean
    public Job job(JobRepository repository, Step step1, Step step2, Step step3, Step step4) {
        return new JobBuilder("StockJob", repository)
                .start(step1)
                .next(step2)
                .next(step3)
                .next(step4)
                .build();
    }

    @Bean
    public NamedParameterJdbcTemplate namedJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    private String extractJobParameter(Map<String, Object> jobParameters, String key, String errorMessage) {
        return Objects.requireNonNull((String) jobParameters.get(key), errorMessage);
    }
}
