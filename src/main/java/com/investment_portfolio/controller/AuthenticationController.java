package com.investment_portfolio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.investment_portfolio.controller.FinClubController;
import com.investment_portfolio.error.InvalidAccessException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
     * Validating whether the incoming request originates from the same machine.
     * <p>If the client IP address does not match the server's IP address, the method logs the mismatch and throws an {@link InvalidAccessException}.</p>
     * @param ip_address the IP address of the client making the request.
     * @param server_ip_address the server's IP address.
     * @throws InvalidAccessException if the client and server IP addresses do not match.
     */
    private void originateFromServer(String ip_address, String server_ip_address) throws InvalidAccessException {
        if (ip_address.equals(server_ip_address)) {
            return;
        }
        String error_message = "The request has been rejected as it does not originate from the server.";
        this.getLogger().error("{}\nClient IP Address: {}\nServer Address: {}", error_message, ip_address, server_ip_address);
        throw new InvalidAccessException(error_message);
    }

    /**
     * Authenticating the user with the external FinClub API, validating that the request originates from the server itself.
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Retrieving the client IP address from the current HTTP request.</li>
     *   <li>Retrieving the server IP address and verifies if the request originates from the server itself.</li>
     *   <li>Constructing a login payload and forwarding it to the FinClub API via the {@link FinClub} model.</li>
     *   <li>Logging each phase and returning an appropriate HTTP response based on success or failure.</li>
     * </ul>
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *   <li>HTTP 200 with the API response body if login is successful.</li>
     *   <li>HTTP 403 if the IP verification fails.</li>
     *   <li>HTTP 500 if the server fails to retrieve its own IP.</li>
     *   <li>HTTP 503 for other unexpected failures during API interaction.</li>
     * </ul>
     */
    @GetMapping("/Login")
    public ResponseEntity<Object> login() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip_address = request.getRemoteAddr();
        this.getLogger().info("The user authentication process has started.");
        try {
            String server_ip_address = InetAddress.getLocalHost().getHostAddress();
            this.originateFromServer(ip_address, server_ip_address);
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
        } catch (UnknownHostException error) {
            int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
            String error_message = "The application cannot retrieve important data.";
            this.getLogger().error("{}\nStatus: {}\nError: {}", error_message, status, error.getMessage());
            return ResponseEntity.status(status).body(Map.of("error", error_message));
        } catch (InvalidAccessException error) {
            int status = HttpStatus.FORBIDDEN.value();
            String error_message = "The user authentication process has failed due to invalid access.";
            this.getLogger().error("{}\nStatus: {}\nError: {}", error_message, status, error.getMessage());
            return ResponseEntity.status(status).body(Map.of("error", error_message));
        } catch (Exception error) {
            int status = HttpStatus.SERVICE_UNAVAILABLE.value();
            String error_message = "The user authentication process has failed.";
            this.getLogger().error("{}\nStatus: {}\nError: {}", error_message, status, error.getMessage());
            return ResponseEntity.status(status).body(Map.of("error", error_message));
        }
    }
}
