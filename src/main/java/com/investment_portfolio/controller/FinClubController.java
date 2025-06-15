package com.investment_portfolio.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Component;


/**
 * Component providing configuration for FinClub-related external uniform resource locators.
 * @author Andy Ewen Gaspard (Darkness4869)
 */
@RestController
@RequestMapping("/FinClub")
public class FinClubController {
    /**
     * The base uniform resource locator that is used by the application to communicate with the service needed.
     */
    private String base_uniform_resource_locator;

    /**
     * The constructor which sets the base uniform resource locator.
     */
    public FinClubController() {
        this.setBaseUniformResourceLocator("https://finclub.mu:8080");
    }

    private String getBaseUniformResourceLocator() {
        return this.base_uniform_resource_locator;
    }

    private void setBaseUniformResourceLocator(String base_uniform_resource_locator) {
        this.base_uniform_resource_locator = base_uniform_resource_locator;
    }
}
