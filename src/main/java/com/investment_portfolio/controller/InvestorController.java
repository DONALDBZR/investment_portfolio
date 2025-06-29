package com.investment_portfolio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.investment_portfolio.controller.FinClubController;
import com.investment_portfolio.model.FinClub;
import java.util.HashMap;
import java.util.Map;
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
}