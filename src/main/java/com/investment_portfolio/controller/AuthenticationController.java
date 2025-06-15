package com.investment_portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Controller responsible for /FinClub/Authentication routes.
 */
@RestController
@RequestMapping("/FinClub/Authentication")
public class AuthenticationController {

    @Value("${external.api.login.url}")
    private String login_api_route;
    private final RestTemplate rest_template = new RestTemplate();
    private final ObjectMapper object_mapper = new ObjectMapper();

    /**
     * Sending login data to an external API, stores the result in a JSON file, and returning the response.
     * @param credentials The login request body from the client
     * @return The response from the external API
     */
    @PostMapping("/Login")
    public ResponseEntity<Object> login(@RequestBody Map<String, Object> credentials) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request_entity = new HttpEntity<>(credentials, headers);
            ResponseEntity<Object> response = this.rest_template.exchange(
                this.login_api_route,
                HttpMethod.POST,
                request_entity,
                Object.class
            );
            this.saveResponseToFile(response.getBody());
            return response;
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", error.getMessage()));
        }
    }

    /**
     * Writing the API response to a JSON file on the server.
     * @param responseBody Response object to write
     * @throws IOException If the file cannot be written
     */
    private void saveResponseToFile(Object responseBody) throws IOException {
        File file = Paths.get("login_response.json").toFile();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, responseBody);
    }
}
