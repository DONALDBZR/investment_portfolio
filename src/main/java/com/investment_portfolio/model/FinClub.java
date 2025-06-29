package com.investment_portfolio.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
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
        } catch (RuntimeException | IOException error) {
            this.getLogger().error("The user authentication process has failed.\nStatus: {}\nError: {}", HttpStatus.SERVICE_UNAVAILABLE.value(), error.getMessage());
            throw new RuntimeException(error.getMessage());
        }
    }

    /**
     * Retrieving user authentication data from cache or via API call.
     * <p>If the cache is valid, data is loaded directly.  Otherwise, a POST request is sent to the external API and the relevant user data is extracted.  The response is then conditionally saved to cache and returned.</p>
     * @param file_status HTTP status indicating the cache file's freshness.
     * @param file_path Path to the local cache file.
     * @param payload The login request payload.
     * @param login_api_route The full API endpoint for login.
     * @return A map containing:
     * <ul>
     *  <li><b>status</b>: HTTP status code indicating outcome.</li>
     *  <li><b>data</b>: Extracted authentication details or {@code null}.</li>
     * </ul>
     * @throws IOException If there are issues reading from or writing to the cache.
     */
    private Map<String, Object> getUserAuthenticationData(
        int file_status,
        String file_path,
        Map<String, Object> payload,
        String login_api_route
    ) throws IOException {
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
        Object api_response_body = api_response.getBody();
        if (api_response_body == null) {
            status = HttpStatus.SERVICE_UNAVAILABLE.value();
            response.put("status", status);
            response.put("data", null);
            this.getLogger().error("The user authentication process has failed due to no data from the API response.\nStatus: {}", status);
            return response;
        }
        Map<String, Object> authentication_data = this.getResponseData(api_response_body);
        if (authentication_data == null || authentication_data.isEmpty()) {
            status = HttpStatus.SERVICE_UNAVAILABLE.value();
            response.put("status", status);
            response.put("data", authentication_data);
            this.getLogger().error("The user authentication process failed due to empty or invalid extracted data.\nStatus: {}", status);
            return response;
        }
        int save_status = this.getFileManager().saveResponseToFile(authentication_data, file_path, http_status);
        boolean is_cached = (http_status == HttpStatus.OK.value());
        boolean is_saved = (save_status == HttpStatus.CREATED.value() || save_status == HttpStatus.ACCEPTED.value());
        status = (is_cached && is_saved) ? HttpStatus.OK.value() : HttpStatus.SERVICE_UNAVAILABLE.value();
        response.put("status", status);
        response.put("data", authentication_data);
        this.getLogger().info("The user authentication process is complete from API request.\nStatus: {}", status);
        return response;
    }

    /**
     * Extracting essential user authentication data from the API response body.
     * <p>This method assumes the response is structured with a "data" object containing a nested "user" object
     * and a top-level "token" field within "data".</p>
     * @param response_body The raw response object from the FinClub login API.
     * @return A map containing selected user details and the token, or {@code null} if the structure is invalid.
     */
    private Map<String, Object> getResponseData(Object response_body) {
        if (!(response_body instanceof Map)) {
            return null;
        }
        Map<String, Object> body = (Map<String, Object>) response_body;
        Object body_data = body.get("data");
        if (!(body_data instanceof Map)) {
            return null;
        }
        Map<String, Object> data = (Map<String, Object>) body_data;
        Object user_data = data.get("user");
        if (!(user_data instanceof Map)) {
            return null;
        }
        Map<String, Object> user = (Map<String, Object>) user_data;
        Map<String, Object> authentication = new HashMap<>();
        authentication.put("ur_id", user.get("ur_id"));
        authentication.put("ur_finid", user.get("ur_finid"));
        authentication.put("ur_firstname", user.get("ur_firstname"));
        authentication.put("ur_middlename", user.get("ur_middlename"));
        authentication.put("ur_surname", user.get("ur_surname"));
        authentication.put("token", data.get("token"));
        return authentication;
    }

    /**
     * Retrieving the user authentication data from a cached file.
     * <p>Validating the provided file path and reads the content using the file manager.</p>
     * @param file_path The path to the cache file containing authentication data.
     * @return The deserialized authentication data as an {@link Object}.
     * @throws IOException If an I/O error occurs while reading the file.
     * @throws FileNotFoundException If the file does not exist.
     */
    public Object getAuthenticationData(String file_path) throws IOException, FileNotFoundException {
        if (file_path == null || file_path.isBlank()) {
            String message = "The file path for reading authentication data is invalid.";
            this.getLogger().error("{}\nFile Path: {}", message, file_path);
            throw new IOException(message);
        }
        return this.getFileManager().readResponseFromFile(file_path);
    }

    /**
     * Retrieving the escrow account overview from the FinClub API.
     * This method sends a GET request to the specified endpoint using the provided bearer token for authorization.  It returns a map containing the HTTP status code and the response data.
     * @param endpoint The uniform resource locator of the escrow account overview API endpoint.
     * @param token The bearer token used for authorization.
     * @return a map with keys "status" (HTTP status code) and "data" (response body).
     */
    public Map<String, Object> getEscrowAccountOverview(String endpoint, String token) {
        this.getLogger().info("The process for communicating with FinClub API to retrieve the escrow account overview has started.");
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Object> api_response = this.getRestTemplate().exchange(endpoint, HttpMethod.GET, request, Object.class);
        int status = api_response.getStatusCodeValue();
        Object response_data = api_response.getBody();
        response.put("status", status);
        response.put("data", response_data);
        this.getLogger().info("The process for communicating with FinClub API to retrieve the escrow account overview has completed.\nStatus: {}", status);
        return response;
    }
}
