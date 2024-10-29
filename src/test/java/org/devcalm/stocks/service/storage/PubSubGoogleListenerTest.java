package org.devcalm.stocks.service.storage;

import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.devcalm.stocks.configuration.properties.StorageProperties;
import org.devcalm.stocks.utils.StringConditions;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.devcalm.stocks.ConstanceHolder.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

class PubSubGoogleListenerTest {

    @Mock
    private Job job;
    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private StorageProperties properties;
    @Mock
    private FirebaseBlobMetaDataExtractor extractor;
    @InjectMocks
    private PubSubGoogleListener listener;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void shouldThrowExceptionWhenMessageIsNull() {
        var emptyHeaders = new MessageHeaders(Map.of());
        var message = mock(Message.class);

        given(message.getHeaders())
                .willReturn(emptyHeaders);

        assertThatThrownBy(() -> listener.messageReceiver(message))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Event type is null");
    }

    @Test
    void shouldThrowExceptionWhenOriginalMessageIsNull() {
        var headers = new MessageHeaders(Map.of("eventType", "test-event"));
        var message = mock(Message.class);

        given(message.getHeaders())
                .willReturn(headers);

        assertThatThrownBy(() -> listener.messageReceiver(message))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Original message is null");
    }

    @Test
    void shouldAckWhenEventIsNotUpload() {
        var acknowledge = mock(BasicAcknowledgeablePubsubMessage.class);
        var headers = new MessageHeaders(Map.of(
                "eventType", "test-event",
                GcpPubSubHeaders.ORIGINAL_MESSAGE, acknowledge));
        var message = mock(Message.class);

        given(message.getHeaders())
                .willReturn(headers);

        listener.messageReceiver(message);

        then(acknowledge)
                .should()
                .ack();
    }

    @Test
    void shouldThrowExceptionWhenObjectIdIsNull() {
        var acknowledge = mock(BasicAcknowledgeablePubsubMessage.class);
        var headers = new MessageHeaders(Map.of("eventType", "OBJECT_FINALIZE",
                GcpPubSubHeaders.ORIGINAL_MESSAGE, acknowledge));
        var message = mock(Message.class);

        given(message.getHeaders())
                .willReturn(headers);

        assertThatThrownBy(() -> listener.messageReceiver(message))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Object ID is null");
    }

    @Test
    void shouldRunJobLauncher() throws Exception {
        var objectId = Instancio.of(String.class).create();
        var acknowledge = mock(BasicAcknowledgeablePubsubMessage.class);
        var headers = new MessageHeaders(Map.of("eventType", "OBJECT_FINALIZE",
                GcpPubSubHeaders.ORIGINAL_MESSAGE, acknowledge,
                "objectId", objectId));
        var message = mock(Message.class);
        var metadata = Instancio.of(StockMetadata.class).create();

        given(message.getHeaders())
                .willReturn(headers);
        given(extractor.extract(objectId))
                .willReturn(metadata);
        given(properties.getLocal())
                .willReturn("storage");
        given(jobLauncher.run(eq(job), any(JobParameters.class)))
                .willAnswer(answer -> {
                    var parameters = (JobParameters) answer.getArgument(1);
                    assertThat(parameters.getString(BATCH_REMOTE_COMPRESSED_FILE)).isEqualTo(objectId);
                    assertThat(parameters.getString(BATCH_LOCAL_COMPRESSED_FILE)).isEqualTo(Path.of("storage", metadata.compressedFile()).toString());
                    assertThat(parameters.getString(BATCH_LOCAL_UNCOMPRESSED_FILE)).isEqualTo(Path.of("storage", metadata.uncompressedFile()).toString());
                    assertThat(parameters.getString(BATCH_STOCK_NAME)).isEqualTo(metadata.name());
                    assertThat(parameters.getString(BATCH_STOCK_START)).isEqualTo(metadata.start().toString());
                    assertThat(parameters.getString(BATCH_STOCK_END)).isEqualTo(metadata.end().toString());
                    assertThat(parameters.getString(BATCH_TRACE_ID)).is(StringConditions.validUUID());
                    return null;
                });

        listener.messageReceiver(message);

        then(acknowledge)
                .should()
                .ack();
    }

    @Test
    void shouldNackWhenServiceThrowException() {
        var objectId = Instancio.of(String.class).create();
        var acknowledge = mock(BasicAcknowledgeablePubsubMessage.class);
        var headers = new MessageHeaders(Map.of("eventType", "OBJECT_FINALIZE",
                GcpPubSubHeaders.ORIGINAL_MESSAGE, acknowledge,
                "objectId", objectId));
        var message = mock(Message.class);

        given(message.getHeaders())
                .willReturn(headers);

        willThrow(new RuntimeException("Incorrect message"))
                .given(extractor)
                .extract(objectId);

        listener.messageReceiver(message);

        then(acknowledge)
                .should()
                .nack();
    }
}