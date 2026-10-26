package online.jamesboggs.guardrail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import online.jamesboggs.guardrail.exception.NotFoundException;
import online.jamesboggs.guardrail.model.Customer;
import online.jamesboggs.guardrail.model.Product;
import online.jamesboggs.guardrail.repository.CustomerRepository;
import online.jamesboggs.guardrail.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only catalog endpoints. @RestController = @Controller + @ResponseBody:
 * whatever a method returns is written to the response as JSON by Jackson.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Catalog", description = "Products and customers used in the demo")
public class CatalogController {

    private final ProductRepository products;
    private final CustomerRepository customers;

    public CatalogController(ProductRepository products, CustomerRepository customers) {
        this.products = products;
        this.customers = customers;
    }

    @GetMapping("/products")
    @Operation(summary = "List products, optionally filtered by product line (Climate, Outdoor, Controls)")
    public List<Product> listProducts(@RequestParam(required = false) String line) {
        return line == null ? products.findAll() : products.findByProductLineIgnoreCase(line);
    }

    @GetMapping("/products/{sku}")
    @Operation(summary = "Get one product by SKU")
    public Product getProduct(@PathVariable String sku) {
        return products.findById(sku)
                .orElseThrow(() -> new NotFoundException("Product not found: " + sku));
    }

    @GetMapping("/customers")
    @Operation(summary = "List customers and their pricing tiers")
    public List<Customer> listCustomers() {
        return customers.findAll();
    }
}
