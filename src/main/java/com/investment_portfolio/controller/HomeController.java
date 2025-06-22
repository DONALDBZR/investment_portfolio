package com.investment_portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



/**
 * Controller responsible for handling the root ("/") path.
 * <p>This controller returns the main homepage view of the application.  The view resolver will render {@code index.html} located in {@code src/main/resources/templates}.</p>
 */
@Controller
public class HomeController {
    /**
     * Handling HTTP GET requests to the root path.
     * @return The name of the HTML view to be rendered, typically {@code index.html}.
     */
    @GetMapping("/")
    public String homepage() {
        return "index";
    }
}
