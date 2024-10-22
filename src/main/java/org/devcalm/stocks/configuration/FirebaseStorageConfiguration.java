package org.devcalm.stocks.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.devcalm.stocks.configuration.properties.GoogleCloudProviderProperties;
import org.devcalm.stocks.exception.StockException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;
import java.io.IOException;

@Profile("!test")
@Configuration
public class FirebaseStorageConfiguration {

    @Bean("firebaseApp")
    public FirebaseApp firebaseApp(GoogleCloudProviderProperties properties) {
        try (FileInputStream serviceAccount = new FileInputStream(properties.getCredential())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(properties.getBucket())
                    .build();
            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new StockException("Firebase application initialization error: " + e.getMessage());
        }
    }

    @Bean
    @DependsOn({"firebaseApp"})
    public Bucket bucket() {
        return StorageClient.getInstance().bucket();
    }
}