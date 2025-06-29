package com.investment_portfolio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import com.investment_portfolio.controller.FinClubController;
import com.investment_portfolio.error.InvalidAccessException;
import com.investment_portfolio.model.FinClub;
import com.investment_portfolio.utility.Error;


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
     * Constructing the payload required for authenticating with the FinClub external API.
     * <p>The payload includes:</p>
     * <ul>
     *  <li>Mode of operation</li>
     *  <li>Sign-in mode</li>
     *  <li>User type</li>
     *  <li>Email and password credentials</li>
     *  <li>Business Registration Number</li>
     * </ul>
     * @return A {@link Map} containing key-value pairs for the login request body.
     */
    private Map<String, Object> getLoginPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("mode", "login");
        payload.put("sign_in_mode", "1");
        payload.put("type", "users");
        payload.put("email", this.getMailAddress());
        payload.put("password", this.getPassword());
        payload.put("brn", "");
        payload.put("type_of", "I");
        return payload;
    }

    /**
     * Handling the user login request by forwarding credentials to the FinClub API.
     * <p>This method performs the following steps:</p>
     * <ul>
     *  <li>Logging the start of the login process.</li>
     *  <li>Building the login request payload.</li>
     *  <li>Forwarding the request to the external FinClub API.</li>
     *  <li>Handling and logging possible exceptions with appropriate HTTP status codes.</li>
     * </ul>
     * @return a {@link ResponseEntity} containing the API response data on success, or an error message with the appropriate HTTP status on failure.
     */
    @GetMapping("/Login")
    public ResponseEntity<Object> login() {
        this.getLogger().info("The user authentication process has started.");
        try {
            Map<String, Object> response = this.getFinClubModel().login(this.getLoginApiRoute(), this.getLoginPayload(), this.getCacheDirectory());
            int status = (int) response.getOrDefault("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            Object data = response.get("data");
            this.getLogger().info("The Login API call is complete.\nStatus: {}", status);
            return ResponseEntity.status(status).body(data);
        } catch (InvalidAccessException error) {
            return Error.handleError(HttpStatus.FORBIDDEN.value(), "The user authentication process has failed due to invalid access.", error);
        } catch (Exception error) {
            return Error.handleError(HttpStatus.SERVICE_UNAVAILABLE.value(), "The user authentication process has failed.", error);
        }
    }
}
