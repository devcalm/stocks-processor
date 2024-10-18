package org.devcalm.stocks.service.batch;

import lombok.RequiredArgsConstructor;
import org.devcalm.stocks.service.storage.UnzipFileService;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.devcalm.stocks.ConstanceHolder.BATCH_LOCAL_COMPRESSED_FILE;
import static org.devcalm.stocks.ConstanceHolder.BATCH_LOCAL_UNCOMPRESSED_FILE;

@Component
@RequiredArgsConstructor
public class UnCompressedStockFileTasklet implements Tasklet {

    private final UnzipFileService unzipFileService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        JobParameters parameters = contribution.getStepExecution().getJobParameters();
        var localCompressedFile = requireNonNull(parameters.getString(BATCH_LOCAL_COMPRESSED_FILE), "Local compressed file cannot be null");
        var localUncompressedFile = requireNonNull(parameters.getString(BATCH_LOCAL_UNCOMPRESSED_FILE), "Uncompressed file cannot be null");

        unzipFileService.unzipFile(localCompressedFile, localUncompressedFile);
        return RepeatStatus.FINISHED;
    }
}
