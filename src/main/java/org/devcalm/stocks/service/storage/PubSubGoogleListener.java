package org.devcalm.stocks.service.storage;

import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devcalm.stocks.configuration.properties.StorageProperties;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;

import static org.devcalm.stocks.ConstanceHolder.*;
import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class PubSubGoogleListener {

    private final Job job;
    private final JobLauncher jobLauncher;
    private final StorageProperties properties;
    private final FirebaseBlobMetaDataExtractor extractor;

    private static final String UPLOAD = "OBJECT_FINALIZE";

    @ServiceActivator(inputChannel = "pubsubInputChannel")
    public void messageReceiver(Message<?> message) {
        var eventType = requireNonNull(message.getHeaders().get("eventType", String.class), "Event type is null");
        var originalMessage = requireNonNull(message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class), "Original message is null");

        if (!eventType.equals(UPLOAD)) {
            originalMessage.ack();
            return;
        }

        var objectId = requireNonNull(message.getHeaders().get("objectId", String.class), "Object ID is null");
        try {
            var metadata = extractor.extract(objectId);
            log.info("Starting job for object {}", objectId);
            jobLauncher.run(job, new JobParametersBuilder()
                    .addString(BATCH_REMOTE_COMPRESSED_FILE, objectId)
                    .addString(BATCH_LOCAL_COMPRESSED_FILE, Path.of(properties.getLocal(), metadata.compressedFile()).toString())
                    .addString(BATCH_LOCAL_UNCOMPRESSED_FILE, Path.of(properties.getLocal(), metadata.uncompressedFile()).toString())
                    .addString(BATCH_STOCK_NAME, metadata.name())
                    .addString(BATCH_STOCK_START, metadata.start().toString())
                    .addString(BATCH_STOCK_END, metadata.end().toString())
                    .addString(BATCH_TRACE_ID, UUID.randomUUID().toString())
                    .toJobParameters());
            originalMessage.ack();
        } catch (Exception e) {
            log.error("Error executing job for object {}", objectId, e);
            originalMessage.nack();
        }
    }
}
