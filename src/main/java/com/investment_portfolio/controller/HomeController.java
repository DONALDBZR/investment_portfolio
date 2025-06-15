package com.investment_portfolio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * HomeController handles HTTP requests to the root ("/") endpoint.
 * <p>This controller is part of the presentation layer (MVC pattern) and provides a simple test route to verify that the application is up and running.
 * </p>
 * @author Andy Ewen Gaspard (Darkness4869)
 */
@RestController
public class HomeController {
    /**
     * Handles HTTP GET requests to the root ("/") path.
     * @return A simple "Hello World" message as a plain text response.
     */
    @GetMapping("/")
    public String helloWorld() {
        return "Hello World";
    }
}
