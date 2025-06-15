package com.investment_portfolio.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;


/**
 * The model is responsible of all of the processing related to the files that are on its server.
 */
public class FileManager {
    /**
     * It is a JSON processing library's class.  It is used for converting between JSON and Java objects and vice-versa.
     */
    private ObjectMapper object_mapper;

    /**
     * Contructing the model by injecting the Object Mapper for the processing of JSON files.
     */
    public FileManager() {
        this.setObjectMapper(new ObjectMapper());
    }

    private ObjectMapper getObjectMapper() {
        return this.object_mapper;
    }

    private void setObjectMapper(ObjectMapper object_mapper) {
        this.object_mapper = object_mapper;
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
