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
import java.nio.file.Path;
import java.util.Map;
import com.investment_portfolio.controller.FinClubController;
import java.util.HashMap;
import com.investment_portfolio.model.FinClub;


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
     * The model responsible for processing requests related to the FinClub external API.
     */
    private FinClub fin_club_model;

    /**
     * Constructing a controller with all required dependencies and credentials for performing authentication requests against the external FinClub API.
     * <p>This constructor initializes:</p>
     * <ul>
     * <li>The login API route, composed from the FinClub base uniform resource locator and the fixed login endpoint.</li>
     * <li>The user credentials injected securely.</li>
     * <li>The cache directory path where API responses may be stored.</li>
     * <li>The HTTP client and JSON processor for communication and serialization tasks.</li>
     * </ul>
     * @param fin_club_controller The injected controller providing the base FinClub API uniform resource locator.
     * @param mail_address The user's email address for API authentication.
     * @param password The user's password for API authentication.
     * @param cache_main_directory The main directory path of the cache.
     */
    @Autowired
    public AuthenticationController(FinClubController fin_club_controller, @Value("${finclub.api.login.mail_address}") String mail_address, @Value("${finclub.api.login.password}") String password, @Value("${cache.path}") String cache_main_directory) {
        this.setMailAddress(mail_address);
        this.setPassword(password);
        this.setLoginApiRoute(fin_club_controller.getBaseUniformResourceLocator() + "/api/WB/authentication/sign-in/");
        this.setCacheDirectory(cache_main_directory + "/authentication");
        this.setFinClubModel(new FinClub());
    }

    private String getLoginApiRoute() {
        return this.login_api_route;
    }

    private void setLoginApiRoute(String login_api_route) {
        this.login_api_route = login_api_route;
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

    private FinClub getFinClubModel() {
        return this.fin_club_model;
    }

    private void setFinClubModel(FinClub fin_club_model) {
        this.fin_club_model = fin_club_model;
    }

    /**
     * Handling user login by sending a request to the external FinClub authentication API and caching the API response locally.
     * <p>This method builds a login payload containing user credentials and other required parameters.  It sends an HTTP POST request to the FinClub API and writes the response to a local JSON file for caching purposes.</p>
     * <p>If both the API request and file caching are successful, the response from the API is returned with HTTP 200 (OK).  If any part of the process fails (including file write failure or HTTP error), a HTTP 503 (Service Unavailable) response is returned along with an appropriate error message.</p>
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *  <li>HTTP 200 with the API response body if login is successful and response is cached</li>
     *  <li>HTTP 503 with an error message if login or caching fails</li>
     * </ul>
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
            Map<String, Object> response = this.getFinClubModel().login(this.getLoginApiRoute(), payload, this.getCacheDirectory());
            int status = (int) response.getOrDefault("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            Object data = response.get("data");
            return ResponseEntity.status(status).body(data);
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "The user authentication has failed.  Please try again later."));
        }
    }
}
