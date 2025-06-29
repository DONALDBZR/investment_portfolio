package com.investment_portfolio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.investment_portfolio.controller.FinClubController;
import com.investment_portfolio.error.InvalidTokenException;
import com.investment_portfolio.model.FinClub;
import com.investment_portfolio.utility.Error;
import java.util.HashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Controller responsible for /FinClub/Investor routes.
 */
@RestController
@RequestMapping("/FinClub/Investor")
public class InvestorController {
    /**
     * The default route portal to be used for all of the investor related actions.
     */
    private String default_route;
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
     * The token of the user.
     */
    private String token;

    /**
     * Constructing the {@code InvestorController} by initializing required configurations.
     * <p>This constructor sets up the default API route for investor account overview, defines the cache directory path, and creates a new {@link FinClub} model instance.</p>
     * @param fin_club_controller The {@code FinClubController} used to derive base uniform resource locators.
     * @param cache_main_directory The root directory where cache data is stored.
     */
    @Autowired
    public InvestorController(FinClubController fin_club_controller, @Value("${cache.path}") String cache_main_directory) {
        this.setLogger(LoggerFactory.getLogger(InvestorController.class));
        this.setDefaultRoute(fin_club_controller.getBaseUniformResourceLocator() + "/api/WB/lenderaccount/overview/investor");
        this.setCacheDirectory(cache_main_directory + "/investor");
        this.setFinClubModel(new FinClub());
        this.getLogger().info("InvestorController initialized.\nDefault Route: {}", this.getDefaultRoute());
    }

    private String getDefaultRoute() {
        return this.default_route;
    }

    private void setDefaultRoute(String default_route) {
        this.default_route = default_route;
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

    private String getToken() {
        return this.token;
    }

    private void setToken(String token) {
        this.token = token;
    }

    /**
     * Retrieving and validating the authentication token from the provided data map.
     * @param data A map containing authentication details, expected to include a "token" key.
     * @return A non-blank authentication token string.
     * @throws IOException If the provided data map is null or empty.
     * @throws InvalidTokenException If the token is missing, blank, or not a valid String.
     */
    private String getAuthenticationToken(Map<String, Object> data) throws IOException, InvalidTokenException {
        if (data == null || data.isEmpty()) {
            String message = "Authentication object is invalid.";
            this.getLogger().error("{}\nAuthentication Data: {}", message, data);
            throw new IOException(message);
        }
        Object token_object = data.get("token");
        if (!(token_object instanceof String token) || token.isBlank()) {
            String message = "The token is invalid.";
            this.getLogger().error("{}\nToken Object: {}", message, token_object);
            throw new InvalidTokenException(message);
        }
        return token;
    }

    /**
     * Retrieving the escrow account overview for the currently authenticated user.
     * <p>This method performs the following steps:</p>
     * <ul>
     *  <li>Loading the authentication data from the local cache</li>
     *  <li>Extracting and sets the JWT token for authorization</li>
     *  <li>Calling the escrow account overview API endpoint using the token</li>
     *  <li>Returning the response data or handles errors appropriately</li>
     * </ul>
     * @return {@link ResponseEntity} containing the response data or error information
     */
    @GetMapping("/EscrowAccountOverview")
    public ResponseEntity<Object> getEscrowAccountOverview() {
        this.getLogger().info("The process for retrieving the escrow account overview has started.");
        try {
            String endpoint = this.getDefaultRoute() + "/getEscrowAccountOverview";
            String authentication_file_path = this.getCacheDirectory() + "/../authentication/response.json";
            Map<String, Object> authentication = (Map<String, Object>) this.getFinClubModel().getAuthenticationData(authentication_file_path);
            this.setToken(this.getAuthenticationToken(authentication));
            Map<String, Object> response = this.getFinClubModel().getEscrowAccountOverview(endpoint, this.getToken());
            int status = (int) response.getOrDefault("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            Object api_data = response.get("data");
            Map<String, Object> data = this.setEscrowAccountOverview((Map<String, Object>) api_data);
            this.getLogger().info("The API call for retrieving the escrow account overview is complete.\nStatus: {}", status);
            return ResponseEntity.status(status).body(data);
        } catch (FileNotFoundException error) {
            return Error.handleError(HttpStatus.NOT_FOUND.value(), "The file does not exist.", error);
        } catch (IOException error) {
            return Error.handleError(HttpStatus.SERVICE_UNAVAILABLE.value(), "The user authentication data is invalid.", error);
        } catch (InvalidTokenException error) {
            return Error.handleError(HttpStatus.FORBIDDEN.value(), "The authentication token is invalid.", error);
        } catch (Exception error) {
            return Error.handleError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occured.", error);
        }
    }

    /**
     * Formating the raw escrow account overview data retrieved from the API into a structured map separating credit and debit details with their corresponding float values.
     * @param api_data The raw data map returned by the escrow account overview API, expected to contain keys, with numeric values as strings.
     * @return A Map containing two nested maps "credit" and "debit" with float values properly parsed.
     */
    private Map<String, Object> setEscrowAccountOverview(Map<String, Object> api_data) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Float> credit = new HashMap<>();
        Map<String, Float> debit = new HashMap<>();
        credit.put("total", this.parseFloat(api_data.get("totalAmountCredit")));
        credit.put("repayment_received", this.parseFloat(api_data.get("totalAmountCreditByEMI")));
        credit.put("amount_transferred", this.parseFloat(api_data.get("totalAmountCreditByYou")));
        debit.put("amount_withdrawn", this.parseFloat(api_data.get("totalAmountDebitByYou")));
        debit.put("amount_disbursed", this.parseFloat(api_data.get("totalAmountInvested")));
        debit.put("total", this.parseFloat(api_data.get("totalAmountDebit")));
        debit.put("amount_withdrawal_pending", this.parseFloat(api_data.get("totaAmountlWithdrawPending")));
        response.put("credit", credit);
        response.put("debit", debit);
        return response;
    }

    /**
     * Parsing the given object into a Float.
     * <p>If the input is null or cannot be parsed as a float, this method returns 0.0f and logs a warning message.</p>
     * @param data the object to parse
     * @return the parsed float value, or 0.0f if parsing fails or data is null
     */
    private Float parseFloat(Object data) {
        if (data == null) {
            return 0.0f;
        }
        try {
            return Float.parseFloat(data.toString());
        } catch (Exception error) {
            this.getLogger().warn("The value cannot be parsed.\nData: {}\nError: {}", data, error.getMessage());
            return 0.0f;
        }
    }
}