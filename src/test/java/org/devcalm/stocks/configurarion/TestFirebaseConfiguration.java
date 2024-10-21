package org.devcalm.stocks.configurarion;

import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Profile("test")
@Configuration
public class TestFirebaseConfiguration {

    @Bean("firebaseApp")
    public FirebaseApp firebaseApp() {
        return mock(FirebaseApp.class);
    }

    @Bean
    public Bucket bucket() {
        return mock(Bucket.class);
    }
}
