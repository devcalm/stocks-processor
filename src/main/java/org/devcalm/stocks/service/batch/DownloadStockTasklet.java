package org.devcalm.stocks.service.batch;

import lombok.RequiredArgsConstructor;
import org.devcalm.stocks.service.storage.FirebaseDownloadFileService;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static org.devcalm.stocks.ConstanceHolder.BATCH_LOCAL_COMPRESSED_FILE;
import static org.devcalm.stocks.ConstanceHolder.BATCH_REMOTE_COMPRESSED_FILE;

@Component
@RequiredArgsConstructor
public class DownloadStockTasklet implements Tasklet {

    private final FirebaseDownloadFileService downloadFileService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        JobParameters parameters = contribution.getStepExecution().getJobParameters();
        String remoteCompressedFile = requireNonNull(parameters.getString(BATCH_REMOTE_COMPRESSED_FILE), "Remote compressed file cannot be null");
        String localCompressedFile = requireNonNull(parameters.getString(BATCH_LOCAL_COMPRESSED_FILE), "Local compressed file cannot be null");

        downloadFileService.downloadFile(remoteCompressedFile, Path.of(localCompressedFile));
        return RepeatStatus.FINISHED;
    }
}
