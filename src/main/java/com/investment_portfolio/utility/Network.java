package com.investment_portfolio.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.investment_portfolio.error.InvalidAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for retrieving network-related information.
 */
public final class Network {
    /**
     * The Start of the private IP Range of the network.
     */
    private static final String ip_address_start = "192.168.8.";
    /**
     * The logger that is responsible of tracking the actions on the application.
     */
    private static final Logger logger = LoggerFactory.getLogger(Network.class);

    public Network() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Determining whether the given IP address is a localhost address.
     * <p>This method checks if the IP address is: </p>
     * <ul>
     *  <li>{@code 127.0.0.1} — IPv4 loopback address</li>
     *  <li>{@code ::1} — IPv6 loopback address</li>
     * </ul>
     * @param ip_address The IP address to validate
     * @return {@code true} if the IP address is a localhost address, {@code false} otherwise
     */
    private static boolean isLocalhost(String ip_address) {
        return "127.0.0.1".equals(ip_address) || "::1".equals(ip_address);
    }

    /**
     * Checking if the given IP address belongs to the server's private network range defined by the IP prefix and a last octet range between 100 and 200 inclusive.
     * @param ip_address The IP address string to check.
     * @return true if the IP address starts with the specified prefix and its last octet is within the private range; false otherwise
     */
    private static boolean isPrivateIpAddress(String ip_address) {
        if (ip_address == null || !ip_address.startsWith(ip_address_start)) {
            return false;
        }
        try {
            int last_octet = Integer.parseInt(ip_address.substring(ip_address.lastIndexOf(".") + 1));
            return last_octet >= 100 && last_octet <= 200;
        } catch (NumberFormatException error) {
            logger.error("The application has failed parsing the IP Address octet.\nIP Address: {}\nError: {}", ip_address, error.getMessage());
            return false;
        }
    }

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

    /**
     * Verifying that the client request originates from a trusted source.
     * <p>The request is considered valid if the client IP address:</p>
     * <ul>
     *  <li>Is a loopback address</li>
     *  <li>Is from a private network</li>
     *  <li>Matches the server's public IP address</li>
     * </ul>
     * If none of the above conditions are met, an {@link InvalidAccessException} is thrown.
     * @param ip_address The IP address of the client making the request.
     * @param server_ip_address The public IP address of the server.
     * @throws InvalidAccessException If the request does not originate from a trusted source.
     */
    public static void originateFromServer(String ip_address, String server_ip_address) throws InvalidAccessException {
        if (ip_address == null || ip_address.isEmpty()) {
            String error_message = "The IP address of the client is missing.";
            logger.error("{}\nClient IP Address: {}\nServer Address: {}", error_message, ip_address, server_ip_address);
            throw new InvalidAccessException(error_message);
        }
        if (isLocalhost(ip_address) || isPrivateIpAddress(ip_address) || ip_address.equals(server_ip_address)) {
            return;
        }
        String error_message = "The request has been rejected as it does not originate from the server.";
        logger.error("{}\nClient IP Address: {}\nServer Address: {}", error_message, ip_address, server_ip_address);
        throw new InvalidAccessException(error_message);
    }
}