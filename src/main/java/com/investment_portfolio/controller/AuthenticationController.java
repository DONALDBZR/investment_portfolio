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
import com.investment_portfolio.model.FinClub;
import com.investment_portfolio.utility.Network;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.io.IOException;


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
        this.setIpAddressStart("192.168.8.");
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
     * Handling user login by authenticating with the FinClub external API.
     * <p>Steps performed:</p>
     * <ul>
     *  <li>Extracting the client's IP address considering the "X-Forwarded-For" header.</li>
     *  <li>Retrieving the server's public IP address.</li>
     *  <li>Verifying the client IP matches the server IP, otherwise throws {@link InvalidAccessException}.</li>
     *  <li>Building and sends the login payload to the FinClub API.</li>
     *  <li>Returning the response or an appropriate error message with HTTP status.</li>
     * </ul>
     * @return {@link ResponseEntity} with login data on success, or error message on failure.
     */
    @GetMapping("/Login")
    public ResponseEntity<Object> login() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip_address = request.getHeader("X-Forwarded-For");
        ip_address = (ip_address != null && !ip_address.isEmpty()) ? ip_address.split(",")[0].trim() : request.getRemoteAddr();
        this.getLogger().info("The user authentication process has started.");
        try {
            String server_ip_address = Network.getServerPublicIp();
            this.getLogger().info("The IP Address of the client will be verified against the IP Address of the server.\nClient IP Address: {}\nServer IP Address: {}", ip_address, server_ip_address);
            Network network_utility = new Network();
            network_utility.originateFromServer(ip_address, server_ip_address);
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
        } catch (IOException error) {
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
