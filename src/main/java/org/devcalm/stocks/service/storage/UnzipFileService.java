package org.devcalm.stocks.service.storage;

import org.devcalm.stocks.exception.StockException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class UnzipFileService {

    private static final int BUFFER_SIZE = 4096;

    public void unzipFile(String zipFilePath, String unzipFilePath) {
        try (var zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = requireNonNull(zipInputStream.getNextEntry(), "Zip entry is null");
            if (entry.isDirectory()) {
                throw new StockException("Directory is not allowed");
            }
            extractFile(zipInputStream, unzipFilePath);
            zipInputStream.closeEntry();

            if (zipInputStream.getNextEntry() != null) {
                zipInputStream.closeEntry();
                throw new StockException("Compressed file contains more than one file");
            }
        } catch (IOException e) {
            throw new StockException("IOException occurred while uncompressed: " + e.getLocalizedMessage());
        }
    }

    private void extractFile(ZipInputStream inputStream, String filePath) throws IOException {
        try (var bos = new BufferedOutputStream(Files.newOutputStream(Path.of(filePath)))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = inputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
