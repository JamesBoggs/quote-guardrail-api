package online.jamesboggs.guardrail.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/** One evaluated quote, saved so every decision has a trail (the audit log). */
@Entity
@Table(name = "quote_records")
public class QuoteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal revenue;

    @Column(name = "gross_margin", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossMargin;

    @Column(name = "margin_pct", nullable = false, precision = 6, scale = 1)
    private BigDecimal marginPct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    @Column(length = 2000)
    private String reasons;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected QuoteRecord() {
        // required by JPA
    }

    public QuoteRecord(String customerId, BigDecimal revenue, BigDecimal grossMargin,
                       BigDecimal marginPct, Decision decision, String reasons) {
        this.customerId = customerId;
        this.revenue = revenue;
        this.grossMargin = grossMargin;
        this.marginPct = marginPct;
        this.decision = decision;
        this.reasons = reasons;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getGrossMargin() { return grossMargin; }
    public BigDecimal getMarginPct() { return marginPct; }
    public Decision getDecision() { return decision; }
    public String getReasons() { return reasons; }
    public Instant getCreatedAt() { return createdAt; }
}
