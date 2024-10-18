package org.devcalm.stocks.service.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;

import static org.devcalm.stocks.ConstanceHolder.BATCH_REMOTE_COMPRESSED_FILE;

@Slf4j
@RequiredArgsConstructor
public class StockJob implements Job {

    private final JobRepository jobRepository;

    @Override
    public String getName() {
        return "StockJob";
    }

    @Override
    public void execute(JobExecution execution) {
        JobParameters jobParameters = execution.getJobParameters();
        String filename = jobParameters.getString(BATCH_REMOTE_COMPRESSED_FILE);
        log.info("Processing stock data from uncompressedFile: {}", filename);
        execution.setStatus(BatchStatus.COMPLETED);
        execution.setExitStatus(ExitStatus.COMPLETED);
        jobRepository.update(execution);
    }
}
