package com.investment_portfolio.model;

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
     * Saving a given response object to a specified path in a human-readable JSON format as well as creating any missing parent directories.
     * @param response The response object to be serialized and written to file.
     * @param file_path The full path to the file where the JSON should be saved.
     * @throws IOException If an I/O error occurs during file operations.
     */
    public void saveResponseToFile(Object response, String file_path) throws IOException {
        Path path = Paths.get(file_path);
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        File file = path.toFile();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, response);
    }
}
