package com.investment_portfolio.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;


/**
 * The model responsible for all processing related to file operations on the server.
 * <p>This class uses the Jackson {@link ObjectMapper} for JSON serialization and provides functionality to save objects as pretty-printed JSON files, ensuring necessary directories are created.</p>
 */
public class FileManager {
    /**
     * Jackson JSON processor used to serialize Java objects to JSON.
     */
    private ObjectMapper object_mapper;

    /**
     * Constructing a new instance initializing the JSON processor.
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
     * Saving the given response object as a pretty-printed JSON file at the specified path.
     * <p>If the target directory does not exist, it will be created automatically.  The method returns an integer status code indicating whether the file was newly created or already existed:</p>
     * <ul>
     * <li>{@code 201} - The file did not exist and was created before writing.</li>
     * <li>{@code 202} - The file already existed and was overwritten.</li>
     * </ul>
     * <p>In case of an I/O error during directory creation or file writing, an {@link IOException} is thrown.</p>
     * @param response The Java object to serialize and save as JSON.
     * @param file_path The full path where the JSON file should be saved.
     * @return {@code 201} if the file was created, {@code 202} if the file already existed and was overwritten.
     * @throws IOException If an I/O error occurs during directory creation or file writing.
     */
    public int saveResponseToFile(Object response, String file_path) throws IOException {
        Path path = Paths.get(file_path);
        boolean fileExisted = Files.exists(path);
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        File file = path.toFile();
        object_mapper.writerWithDefaultPrettyPrinter().writeValue(file, response);
        return (fileExisted) ? 202 : 201;
    }
}
