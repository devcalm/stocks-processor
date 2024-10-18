package org.devcalm.stocks.service.batch;

import lombok.RequiredArgsConstructor;
import org.devcalm.stocks.service.storage.DeleteFileService;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static org.devcalm.stocks.ConstanceHolder.*;
import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class CleanUpTasklet implements Tasklet {

    private final DeleteFileService deleteFileService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        JobParameters parameters = contribution.getStepExecution().getJobParameters();

        var remoteCompressedFile = requireNonNull(parameters.getString(BATCH_REMOTE_COMPRESSED_FILE), "Remote compressed file not set");
        var localCompressedFile = requireNonNull(parameters.getString(BATCH_LOCAL_COMPRESSED_FILE), "Local compressed file not set");
        var localUncompressedFile = requireNonNull(parameters.getString(BATCH_LOCAL_UNCOMPRESSED_FILE), "Local unzip uncompressed not set");

        deleteFileService.deleteLocal(Path.of(localCompressedFile));
        deleteFileService.deleteLocal(Path.of(localUncompressedFile));
        deleteFileService.deleteRemote(remoteCompressedFile);

        return RepeatStatus.FINISHED;
    }
}
