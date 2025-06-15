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
     * Sending a login request with user credentials to an external FinClub authentication API, saving the JSON response to a file on the server, and returning the API response to the client.
     * <p>The request payload includes necessary fields such as mode, sign-in mode, user type, email, password, and other required parameters.  The request is sent as a POST with a JSON body.</p>
     * <p>If the external API call succeeds, the JSON response body is saved in a local cache file.  If any exception occurs during the process the method
     * returns a 503 Service Unavailable status with an error message.</p>
     * @return a {@link ResponseEntity} containing the external API's response body on success, or an error message with HTTP status 503 if the request fails.
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
            Object response = this.getFinClubModel().login(this.getLoginApiRoute(), payload, this.getCacheDirectory());
            return ResponseEntity.ok(response);
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "The user authentication has failed.  Please try again later."));
        }
    }
}
