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
     * Authenticating the user by either retrieving a cached session or performing a login request to the FinClub API.
     * <p>The method attempts to validate and reuse an existing cache file.  If the file is invalid or expired, a new API request is sent, and the response is cached locally.  The result is returned as a structured map.</p>
     * <h3>Operations Performed:</h3>
     * <ul>
     *  <li>Checks if the cached authentication file is valid.</li>
     *  <li>If valid, returns cached response.  Otherwise, makes a POST request to the login API.</li>
     *  <li>Caches the API response to a file for future use.</li>
     * </ul>
     * @param login_api_route The complete uniform resource locator to the FinClub login API endpoint.
     * @param payload A map containing the login request body.
     * @param cache_directory The directory path where the authentication response should be cached.
     * @return A {@link Map} with:
     * <ul>
     *  <li><b>{@code status}</b>: An HTTP-style status code indicating the outcome.</li>
     *  <li><b>{@code data}</b>: The response body either from the cache or the API.</li>
     * </ul>
     * @throws RuntimeException If an I/O error or an unexpected failure occurs during the login or caching process.
     */
    public Map<String, Object> login(String login_api_route, Map<String, Object> payload, String cache_directory) throws RuntimeException {
        this.getLogger().info("The user authentication process has started.");
        try {
            String file_path = cache_directory + "/response.json";
            int file_status = this.getFileManager().isValidPath(file_path);
            return this.getUserAuthenticationData(file_status, file_path, payload, login_api_route);
        } catch (RuntimeException error) {
            this.getLogger().error("The user authentication process has failed.\nStatus: {}\nError: {}", HttpStatus.SERVICE_UNAVAILABLE.value(), error.getMessage());
            throw new RuntimeException(error.getMessage());
        }
    }

    /**
     * Retrieving user authentication data from a cached file or performs a login request to the FinClub API.
     * <p>If the cached file is still valid, the response is read directly from the cache.  Otherwise, a login API call is made using the given payload, and the response is saved to the specified cache file.</p>
     * @param file_status HTTP-style status code indicating the validity of the cached file.
     * @param file_path Full path to the cache file containing the authentication response.
     * @param payload A map containing login credentials and required parameters for the authentication API.
     * @param login_api_route The uniform resource locator of the FinClub login API endpoint.
     * @return A map containing:
     * <ul>
     *  <li><b>{@code status}</b>: HTTP-style response code indicating the result of the operation.</li>
     *  <li><b>{@code data}</b>: The user authentication data retrieved either from the cache or the API.</li>
     * </ul>
     * @throws IOException If the file operations encounter an unrecoverable error.
     * @throws RuntimeException If the API request encounter an unrecoverable error.
     */
    private Map<String, Object> getUserAuthenticationData(
        int file_status,
        String file_path,
        Map<String, Object> payload,
        String login_api_route
    ) throws IOException, RuntimeException {
        int status;
        Map<String, Object> response = new HashMap<>();
        if (file_status == HttpStatus.OK.value()) {
            status = HttpStatus.FOUND.value();
            response.put("status", file_status);
            response.put("data", this.getFileManager().readResponseFromFile(file_path));
            this.getLogger().info("The user authentication process is complete using valid cached session.\nStatus: {}", status);
            return response;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Object> api_response = this.getRestTemplate().exchange(login_api_route, HttpMethod.POST, request, Object.class);
        int http_status = api_response.getStatusCodeValue();
        int save_status = this.getFileManager().saveResponseToFile(api_response.getBody(), file_path, http_status);
        status = (http_status == HttpStatus.OK.value() && (save_status == HttpStatus.CREATED.value() || save_status == HttpStatus.ACCEPTED.value())) ? HttpStatus.OK.value() : HttpStatus.SERVICE_UNAVAILABLE.value();
        response.put("status", status);
        response.put("data", api_response.getBody());
        this.getLogger().info("The user authentication process is complete from API request.\nStatus: {}", status);
        return response;
    }
}
