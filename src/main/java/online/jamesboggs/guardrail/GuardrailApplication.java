package online.jamesboggs.guardrail;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point. @SpringBootApplication turns on component scanning (finds the
 * controllers, services and repositories in this package) and auto-configuration
 * (sets up the web server, Jackson for JSON, and the H2 database).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(info = @Info(
        title = "Quote Guardrail API",
        version = "1.0",
        description = "Checks a quote against margin floors and discount limits and returns a decision: "
                + "APPROVE, ROUTE_TO_VP or BLOCK. Demo data only."))
public class GuardrailApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuardrailApplication.class, args);
    }
}
