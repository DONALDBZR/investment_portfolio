package com.investment_portfolio;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * The main entry point of the Investment Portfolio application.
 * <p>This is a Spring Boot application that does not require any database connection or JPA configuration. The {@code DataSourceAutoConfiguration} and {@code HibernateJpaAutoConfiguration} auto-configurations are excluded to prevent Spring Boot from attempting to set up a data source.
 * <p>To start the application, execute the {@code main} method.
 */
@SpringBootApplication(
	exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
	}
)
public class IndexApplication {
	/**
     * Launching the Spring Boot application.
     * @param args optional command-line arguments
     */
	public static void main(String[] args) {
		SpringApplication.run(IndexApplication.class, args);
	}
}
