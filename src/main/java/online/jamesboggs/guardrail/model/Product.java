package online.jamesboggs.guardrail.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * A product in the catalog. @Entity maps this class to a database table;
 * Spring Data JPA handles the SQL for reading and writing it.
 * Money uses BigDecimal, never double, so prices don't pick up rounding errors.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(name = "product_line", nullable = false)
    private String productLine;

    @Column(name = "list_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    protected Product() {
        // required by JPA
    }

    public Product(String sku, String name, String productLine, BigDecimal listPrice, BigDecimal unitCost) {
        this.sku = sku;
        this.name = name;
        this.productLine = productLine;
        this.listPrice = listPrice;
        this.unitCost = unitCost;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getProductLine() { return productLine; }
    public BigDecimal getListPrice() { return listPrice; }
    public BigDecimal getUnitCost() { return unitCost; }
}
