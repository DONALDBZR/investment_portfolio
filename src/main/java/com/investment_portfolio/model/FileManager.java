package com.investment_portfolio.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;


/**
 * The model is responsible of all of the processing related to the files that are on its server.
 */
public class FileManager {
    private ObjectMapper object_mapper;

    public FileManager() {
        this.object_mapper = new ObjectMapper();
    }

    /**
     * Saves the given object as a JSON file at the specified directory.
     * If the directory does not exist, it will be created.
     *
     * @param directoryPath The directory to save the file in.
     * @param fileName The name of the file to write.
     * @param data The object to serialize and save.
     * @throws IOException If any I/O error occurs.
     */
    public void saveJsonToFile(String directoryPath, String fileName, Object data) throws IOException {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        File file = Paths.get(directoryPath, fileName).toFile();
        this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
    }
}
