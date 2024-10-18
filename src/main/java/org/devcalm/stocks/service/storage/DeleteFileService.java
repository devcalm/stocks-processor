package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.exception.StockException;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class DeleteFileService {

    private final Bucket bucket;

    public void deleteLocal(Path filename) {
        try {
            Files.delete(filename);
        } catch (IOException e) {
            throw new StockException("Cannot delete local uncompressed file: " + filename);
        }
    }

    public void deleteRemote(String filename) {
        Blob blob = bucket.get(filename);
        blob.delete();
    }
}
