package com.investment_portfolio.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**
 * Utility class for retrieving network-related information.
 */
public class Network {
    /**
     * Retrieving the server's current public IP address by querying the ipify API.
     * 
     * <p>This method uses a simple HTTP GET request to fetch the public IP from <a href="https://api.ipify.org">https://api.ipify.org</a>.</p>
     * @return A {@link String} representing the public IP address of the server.
     * @throws IOException If an I/O error occurs while making the HTTP request.
     */
    public static String getServerPublicIp() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://api.ipify.org").openStream(), StandardCharsets.UTF_8))) {
            return reader.readLine();
        }
    }
}