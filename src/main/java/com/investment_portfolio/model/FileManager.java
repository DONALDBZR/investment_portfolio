package com.investment_portfolio.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;


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
     * The logger that is responsible of tracking the actions on the application.
     */
    private Logger logger;

    /**
     * Constructing a new {@code FileManager} instance and initializing the necessary components for file operations and JSON processing.
     * <p>Specifically, this constructor performs the following:</p>
     * <ul>
     *  <li>Initializing a SLF4J logger specific to the {@code FileManager} class for logging internal operations.</li>
     *  <li>Instantiating a Jackson {@code ObjectMapper} for serializing and deserializing JSON data.</li>
     * </ul>
     * <p>A log message is emitted to confirm successful initialization.</p>
     */
    public FileManager() {
        this.setLogger(LoggerFactory.getLogger(FileManager.class));
        this.setObjectMapper(new ObjectMapper());
        this.getLogger().info("File Manager Model initialized.");
    }

    private ObjectMapper getObjectMapper() {
        return this.object_mapper;
    }

    private void setObjectMapper(ObjectMapper object_mapper) {
        this.object_mapper = object_mapper;
    }

    private Logger getLogger() {
        return this.logger;
    }

    private void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Serializing the provided response object into a pretty-printed JSON format and writing it to the specified file path.
     * <p>If the directory structure does not exist, it will be created automatically.  The method determines whether the target file already existed and returns a corresponding HTTP-style status code to indicate the outcome of the write operation:</p>
     * <ul>
     *  <li>{@code 201 (Created)} – The file did not exist before and was created successfully.</li>
     *  <li>{@code 202 (Accepted)} – The file already existed and was overwritten successfully.</li>
     * </ul>
     * <p>Detailed log messages are generated throughout the process to aid in debugging and traceability.</p>
     * @param response The Java object to be serialized and saved as JSON.
     * @param file_path The absolute path where the JSON file should be written.
     * @return An integer HTTP-style status code indicating whether the file was created or overwritten.
     * @throws IOException If an error occurs during directory creation or while writing the file.
     */
    public int saveResponseToFile(Object response, String file_path) throws IOException {
        this.getLogger().info("The process for writing the data in the cache has started.\nFile Path: {}", file_path);
        Path path = Paths.get(file_path);
        boolean fileExisted = Files.exists(path);
        if (!Files.exists(path.getParent())) {
            this.getLogger().info("Creating the missing directory.\nDirectory: {}", path.getParent());
            Files.createDirectories(path.getParent());
        }
        File file = path.toFile();
        object_mapper.writerWithDefaultPrettyPrinter().writeValue(file, response);
        int status = (fileExisted) ? 202 : 201;
        this.getLogger().info("The file has been modified.\nStatus: {}", status);
        return status;
    }

    /**
     * Validating whether a file exists and whether its creation time is within the allowed time window.
     * <p>This method checks for the existence of the file and verifies if the file was created within the last hour.</p>
     * @param file_path The full path to the file to validate.
     * @return An HTTP-style status code:
     * <ul>
     *  <li><b>200</b> – File exists and is within the valid time window.</li>
     *  <li><b>403</b> – File exists but has expired based on creation time.</li>
     *  <li><b>404</b> – File does not exist.</li>
     *  <li><b>500</b> – Internal error occurred while reading file attributes.</li>
     * </ul>
     */
    public int isValidPath(String file_path) {
        try {
            long current_time = Instant.now().getEpochSecond();
            Path path = Paths.get(file_path);
            this.fileExists(path);
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            long creation_time = attributes.creationTime().toInstant().getEpochSecond();
            long valid_until = creation_time + 3600;
            this.fileValid(current_time, valid_until);
            return 200;
        } catch (FileNotFoundException error) {
            int status = 404;
            this.getLogger().warn("The file path cannot be validated.\nFile Path: {}\nStatus: {}\nWarning: {}", file_path, status, error.getMessage());
            return status;
        } catch (AccessDeniedException error) {
            int status = 403;
            this.getLogger().warn("The file path cannot be validated.\nFile Path: {}\nStatus: {}\nWarning: {}", file_path, status, error.getMessage());
            return status;
        // } catch (IOException | InvalidPathException error) {
        //     this.getLogger().error("The file path cannot be validated.\nStatus: {}\nError: {}", 500, error.getMessage());
        //     throw new RuntimeException(error.getMessage());
        }
    }

    /**
     * Checking whether the specified file path exists.
     * <p>If the file does not exist, a {@link FileNotFoundException} is thrown.</p>
     * @param file_path The {@link Path} to the file to check.
     * @throws FileNotFoundException If the file does not exist at the specified path.
     */
    private void fileExists(Path file_path) throws FileNotFoundException {
        if (Files.exists(file_path)) {
            return;
        }
        throw new FileNotFoundException("The file path does not exist.");
    }

    /**
     * Validating whether the file's data is still considered valid based on the given time constraints.
     * <p>If the current time has passed the validity threshold, an {@link AccessDeniedException} is thrown.</p>
     * @param current_time The current UNIX timestamp in seconds.
     * @param valid_until The UNIX timestamp until which the file is considered valid.
     * @throws AccessDeniedException If the current time exceeds the validity threshold.
     */
    private void fileValid(long current_time, long valid_until) throws AccessDeniedException {
        if (current_time <= valid_until) {
            return;
        }
        throw new AccessDeniedException("The data in the file is not valid to be used.");
    }
}
