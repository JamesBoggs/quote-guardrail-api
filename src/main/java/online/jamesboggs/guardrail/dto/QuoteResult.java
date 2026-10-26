package online.jamesboggs.guardrail.dto;

import online.jamesboggs.guardrail.model.Decision;

import java.math.BigDecimal;
import java.util.List;

/** The JSON response for an evaluated quote. */
public record QuoteResult(
        Long quoteId,
        String customerId,
        String customerTier,
        Decision decision,
        List<String> reasons,
        BigDecimal revenue,
        BigDecimal cost,
        BigDecimal grossMargin,
        BigDecimal marginPct,
        List<LineResult> lines) {

    public record LineResult(
            String sku,
            String name,
            int quantity,
            BigDecimal listPrice,
            BigDecimal discountPct,
            BigDecimal netPrice,
            BigDecimal revenue,
            BigDecimal cost,
            BigDecimal marginPct) {
    }
}
