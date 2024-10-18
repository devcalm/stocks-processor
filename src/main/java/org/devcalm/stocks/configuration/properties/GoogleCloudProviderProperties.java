package org.devcalm.stocks.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties("gcp")
public class GoogleCloudProviderProperties {
    @NotBlank
    private String credential;
    @NotBlank
    private String subscription;
    @NotBlank
    private String projectId;
    @NotBlank
    private String bucket;
}
