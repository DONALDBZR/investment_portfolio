package com.investment_portfolio.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
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
     * The logger that is responsible of tracking the actions on the application.
     */
    private Logger logger;

    /**
     * Constructing a new instance of the {@code FinClub} model by initializing its core dependencies:
     * <ul>
     *  <li>A {@link RestTemplate} for communicating with external HTTP services.</li>
     *  <li>A {@link FileManager} for handling local file I/O operations.</li>
     *  <li>A {@link Logger} instance for logging events and diagnostics.</li>
     * </ul>
     * <p>Logging an informational message when the model is successfully initialized.</p>
     */
    public FinClub() {
        this.setLogger(LoggerFactory.getLogger(FinClub.class));
        this.setRestTemplate(new RestTemplate());
        this.setFileManager(new FileManager());
        this.getLogger().info("FinClub Model initialized.");
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

    private Logger getLogger() {
        return this.logger;
    }

    private void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sending a login request to the FinClub API using the provided endpoint and payload, and caching the response body to a JSON file in the specified directory.
     * <p>The method performs the following operations:</p>
     *  <ul>
     *      <li>Building an HTTP POST request using the provided login payload.</li>
     *      <li>Sending the request to the FinClub login endpoint.</li>
     *      <li>Caching the response body to a file located in the given cache directory.</li>
     *      <li>Determining the final status based on the HTTP response and file write result.</li>
     * </ul>
     * <p>If both the API call and file caching are successful, the method returns an OK status.  Otherwise, it returns a Service Unavailable status.  If any exception occurs during the request or file operation, a {@link RuntimeException} is thrown and the error is logged.</p>
     * @param login_api_route The fully qualified uniform resource locator of the FinClub login API.
     * @param payload A map containing login credentials and parameters required by the API.
     * @param cache_directory The local directory path where the API response will be cached.
     * @return a {@link Map} containing:
     * <ul>
     *  <li><b>{@code status}</b>: An integer HTTP status code.</li>
     *  <li><b>{@code data}</b>: The API response body.</li>
     * </ul>
     * @throws RuntimeException If an unrecoverable I/O or HTTP error occurs.
     */
    public Map<String, Object> login(String login_api_route, Map<String, Object> payload, String cache_directory) throws RuntimeException {
        this.getLogger().info("The user authentication process has started.");
        Map<String, Object> response = new HashMap<>();
        try {
            String file_path = cache_directory + "/response.json";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Object> api_response = this.getRestTemplate().exchange(
                login_api_route,
                HttpMethod.POST,
                request,
                Object.class
            );
            int http_status = api_response.getStatusCodeValue();
            int file_status = this.getFileManager().saveResponseToFile(api_response.getBody(), file_path);
            int status = (http_status == HttpStatus.OK.value() && (file_status == HttpStatus.CREATED.value() || file_status == HttpStatus.ACCEPTED.value())) ? HttpStatus.OK.value() : HttpStatus.SERVICE_UNAVAILABLE.value();
            response.put("status", status);
            response.put("data", api_response.getBody());
            this.getLogger().info("The user authentication process is complete.\nStatus: {}", status);
            return response;
        } catch (IOException error) {
            this.getLogger().error("The user authentication process has failed.\nStatus: 503\nError: {}", error.getMessage());
            throw new RuntimeException(error.getMessage());
        }
    }
}
