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
     * Sending a login request to the FinClub API with the specified endpoint and payload, then caching the response as a JSON file in the given cache directory.
     * <p>This method constructs an HTTP POST request with the login credentials, sends it to the FinClub login API, and writes the response body to a file.  The method returns a map containing the HTTP status and response data.</p>
     * @param login_api_route The complete uniform resource locator of the FinClub login endpoint.
     * @param payload A map containing the login credentials and any additional parameters.
     * @param cache_directory The path to the directory where the response should be cached.
     * @return A map with the following keys:
     * <ul>
     *  <li><b>{@code status}</b>: HTTP status code — 200 (OK) on success, 503 (Service Unavailable) on failure.</li>
     *  <li><b>{@code data}</b>: The response body from the FinClub API, or the error message if an exception occurred.</li>
     * </ul>
     * @throws RuntimeException if any unrecoverable error occurs during the request or file operation.
     */
    public Map<String, Object> login(String login_api_route, Map<String, Object> payload, String cache_directory) {
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
            return response;
        } catch (Exception error) {
            response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            response.put("data", error.getMessage());
            return response;
        }
    }
}
