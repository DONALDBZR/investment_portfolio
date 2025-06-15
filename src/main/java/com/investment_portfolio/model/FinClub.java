package com.investment_portfolio.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import com.investment_portfolio.model.FileManager;


/**
 * Model responsible for processing requests related to the FinClub external API.
 */
@Component
public class FinClub {
    /**
     * It allows the application to act as a client and interact with external REST APIs.
     */
    private RestTemplate rest_template;
    /**
     * The model is responsible of all of the processing related to the files that are on its server.
     */
    private FileManager file_manager;

    /**
     * Contructing the model by injecting the REST Template which will allow the model to communicate with external services as well as injecting the Object Mapper which will handle the JSON processing.
     */
    public FinClub() {
        this.setRestTemplate(new RestTemplate());
        this.setFileManager(new FileManager());
    }

    private RestTemplate getRestTemplate() {
        return this.rest_template;
    }

    private void setRestTemplate(RestTemplate rest_template) {
        this.rest_template = rest_template;
    }

    private FileManager getFileManager() {
        return this.file_manager;
    }

    private void setFileManager(FileManager file_manager) {
        this.file_manager = file_manager;
    }

    /**
     * Sending a login request to the FinClub API using the provided endpoint and payload.
     * @param login_api_route The full uniform resource locator to the FinClub login endpoint.
     * @param payload The request body containing login credentials and parameters.
     * @param cache_directory The directory path of the cache.
     * @return Object The response body returned from the FinClub API.
     */
    public Object login(String login_api_route, Map<String, Object> payload, String cache_directory) {
        String file_path = cache_directory + "/response.json";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Object> response = this.getRestTemplate().exchange(
            login_api_route,
            HttpMethod.POST,
            request,
            Object.class
        );
        this.saveResponseToFile(response.getBody(), file_path);
        return response.getBody();
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
