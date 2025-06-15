package com.investment_portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Controller responsible for /FinClub/Authentication routes.
 */
@RestController
@RequestMapping("/FinClub/Authentication")
public class AuthenticationController {
    /**
     * The route to be used for authenticating with the Peer-to-Peer Lending Platform.
     */
    private String login_api_route;
    /**
     * It allows the application to act as a client and interact with external REST APIs.
     */
    private RestTemplate rest_template;
    /**
     * It is a JSON processing library's class.  It is used for converting between JSON and Java objects and vice-versa.
     */
    private ObjectMapper object_mapper;

    /**
     * The constructor of the controller which sets the data needed for the authentication.
     */
    @Autowired
    public AuthenticationController() {
        this.setLoginApiRoute("https://finclub.mu:8080/api/WB/authentication/sign-in/");
        this.setRestTemplate(new RestTemplate());
        this.setObjectMapper(new ObjectMapper());
    }

    private String getLoginApiRoute() {
        return this.login_api_route;
    }

    private void setLoginApiRoute(String login_api_route) {
        this.login_api_route = login_api_route;
    }

    private RestTemplate getRestTemplate() {
        return this.rest_template;
    }

    private void setRestTemplate(RestTemplate rest_template) {
        this.rest_template = rest_template;
    }

    private ObjectMapper getObjectMapper() {
        return this.object_mapper;
    }

    private void setObjectMapper(ObjectMapper object_mapper) {
        this.object_mapper = object_mapper;
    }

    /**
     * Sending login data to an external API, storing the result in a JSON file, and returning the response.
     * @param credentials The login request body from the client
     * @return The response from the external API
     */
    @PostMapping("/Login")
    public ResponseEntity<Object> login(@RequestBody Map<String, Object> credentials) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request_entity = new HttpEntity<>(credentials, headers);
            ResponseEntity<Object> response = this.getRestTemplate().exchange(
                this.getLoginApiRoute(),
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
     * @param response_body Response object to write
     * @throws IOException If the file cannot be written
     */
    private void saveResponseToFile(Object response_body) throws IOException {
        File file = Paths.get("login_response.json").toFile();
        this.object_mapper.writerWithDefaultPrettyPrinter().writeValue(file, response_body);
    }
}
