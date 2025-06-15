package com.investment_portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * The logger that is responsible of tracking the actions on the application.
     */
    private Logger logger;

    /**
     * Constructing an {@link AuthenticationController} with the necessary configuration to interact with the FinClub external API.
     * <p>This constructor performs the following initializations:</p>
     * <ul>
     *  <li>Configuring the logger instance for monitoring and debugging activities within the controller.</li>
     *  <li>Assembling the full login API uniform resource locator using the base URI provided by {@code fin_club_controller} and a predefined login endpoint.</li>
     *  <li>Injecting the user credentials securely from the application's configuration.</li>
     *  <li>Defining the specific subdirectory within the cache system for storing authentication responses.</li>
     *  <li>Instantiating a {@link FinClub} model used for API communication and business logic.</li>
     * </ul>
     * <p>A startup log entry is recorded indicating the initialization of the controller and the login endpoint in use.</p>
     * @param fin_club_controller The controller providing the FinClub API base URI.
     * @param mail_address The user email address for authenticating with the external API.
     * @param password The user password for authenticating with the external API.
     * @param cache_main_directory The root directory under which authentication responses will be cached.
     */
    @Autowired
    public AuthenticationController(
        FinClubController fin_club_controller,
        @Value("${finclub.api.login.mail_address}") String mail_address,
        @Value("${finclub.api.login.password}") String password,
        @Value("${cache.path}") String cache_main_directory
    ) {
        this.setLogger(LoggerFactory.getLogger(AuthenticationController.class));
        this.setMailAddress(mail_address);
        this.setPassword(password);
        this.setLoginApiRoute(fin_club_controller.getBaseUniformResourceLocator() + "/api/WB/authentication/sign-in/");
        this.setCacheDirectory(cache_main_directory + "/authentication");
        this.setFinClubModel(new FinClub());
        this.getLogger().info("AuthenticationController initialized.\nLogin API Route: {}", this.getLoginApiRoute());
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

    private Logger getLogger() {
        return this.logger;
    }

    private void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Handling user login by sending authentication credentials to the external FinClub API and caching the response locally.
     * <p>This method performs the following steps:</p>
     * <ul>
     *  <li>Building a login payload with user credentials and required parameters.</li>
     *  <li>Performing an HTTP POST request to the FinClub authentication endpoint.</li>
     *  <li>Caching the response from the API to a local directory (if successful).</li>
     * </ul>
     * <p>If the authentication and caching succeed, the API response is returned with an HTTP 200 (OK) status.  If any exception occurs, the method logs the error and returns an HTTP 503 (Service Unavailable) response.</p>
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *  <li>HTTP 200 and the API response body if login is successful</li>
     *  <li>HTTP 503 and a descriptive error message if login or caching fails</li>
     * </ul>
     */
    @PostMapping("/Login")
    public ResponseEntity<Object> login() {
        this.getLogger().info("The user authentication process has started");
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
            this.getLogger().info("The Login API call is complete.\nStatus: {}", status);
            return ResponseEntity.status(status).body(data);
        } catch (Exception error) {
            this.getLogger().error("The user authentication process has failed.\nStatus: {}\nError: {}", HttpStatus.SERVICE_UNAVAILABLE.value(), error.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value()).body(Map.of("error", "The user authentication process has failed.  Please try again later."));
        }
    }
}
