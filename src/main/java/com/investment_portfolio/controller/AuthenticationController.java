package com.investment_portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import com.investment_portfolio.controller.FinClubController;
import java.util.HashMap;


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
     * The mail address of the user.
     */
    private String mail_address;
    /**
     * The password of the user.
     */
    private String password;
    /**
     * The path of the cache directory that is used by the controller.
     */
    private String cache_directory;

    /**
     * Constructing an {@code AuthenticationController} instance with the necessary dependencies and credentials to perform authentication requests against the external FinClub API.
     * <p>This constructor initializes the login API route using the base uniform resource locator from the injected controller, and retrieves the login credentials from the application configuration.  It also initializes the required components for HTTP requests and JSON processing.</p>
     * @param fin_club_controller The injected controller containing the base FinClub API uniform resource locator.
     * @param mail_address The user's email address used for authentication.
     * @param password The user's password used for authentication.
     */
    @Autowired
    public AuthenticationController(FinClubController fin_club_controller, @Value("${finclub.api.login.mail_address}") String mail_address, @Value("${finclub.api.login.password}") String password) {
        String login_api_route = fin_club_controller.getBaseUniformResourceLocator() + "/api/WB/authentication/sign-in/";
        this.setMailAddress(mail_address);
        this.setPassword(password);
        this.setLoginApiRoute(login_api_route);
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

    private String getMailAddress() {
        return this.mail_address;
    }

    private void setMailAddress(String mail_address) {
        this.mail_address = mail_address;
    }

    private String getPassword() {
        return this.password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    private String getCacheDirectory() {
        return this.cache_directory;
    }

    private void setCacheDirectory(String cache_directory) {
        this.cache_directory = cache_directory;
    }

    /**
     * Sending login data to an external API, storing the result in a JSON file, and returning the response.
     * @return The response from the external API
     */
    @PostMapping("/Login")
    public ResponseEntity<Object> login() {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("mode", "login");
            payload.put("sign_in_mode", "1");
            payload.put("type", "users");
            payload.put("email", this.getMailAddress());
            payload.put("password", this.getPassword());
            payload.put("brn", "");
            payload.put("type_of", "I");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Object> response = this.getRestTemplate().exchange(
                this.getLoginApiRoute(),
                HttpMethod.POST,
                request,
                Object.class
            );
            this.saveResponseToFile(response.getBody());
            return response;
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", error.getMessage()));
        }
    }

    /**
     * Persisting the given API response object to a JSON file on the server in a human-readable format.
     * <p>The response is serialized using the configured {@link ObjectMapper} and written to the file.  If the target directory does not exist, it will be automatically created.</p>
     * @param response_body The API response object to serialize and store as JSON.
     * @throws IOException If an I/O error occurs during directory creation or file writing.
     */
    private void saveResponseToFile(Object response_body) throws IOException {
        String file_path = this.getCacheDirectory() + "/response.json";
        Path directory = Paths.get(this.getCacheDirectory());
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        File file = Paths.get(file_path).toFile();
        this.getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, response_body);
    }
}
