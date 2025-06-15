package com.investment_portfolio.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


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
     * @param base_uniform_resource_locator The base uniform resource locator for FinClub API.
     */
    public FinClubController(@Value("${finclub.api.base-uniform-resource-locator}") String base_uniform_resource_locator) {
        this.setBaseUniformResourceLocator(base_uniform_resource_locator);
    }

    public String getBaseUniformResourceLocator() {
        return this.base_uniform_resource_locator;
    }

    private void setBaseUniformResourceLocator(String base_uniform_resource_locator) {
        this.base_uniform_resource_locator = base_uniform_resource_locator;
    }
}
