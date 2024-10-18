package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.exception.StockException;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class FirebaseDownloadFileService {

    private final Bucket bucket;

    public void downloadFile(String remoteFilePath, Path destinationPath) {
        Blob blob = bucket.get(remoteFilePath);
        if (blob == null) {
            throw new StockException("File %s not found in the bucket".formatted(remoteFilePath));
        }
        try (FileOutputStream fos = new FileOutputStream(destinationPath.toFile())) {
            blob.downloadTo(fos);
        } catch (IOException i) {
            throw new StockException("Error while downloading compressed file: %s".formatted(remoteFilePath));
        }
    }
}
