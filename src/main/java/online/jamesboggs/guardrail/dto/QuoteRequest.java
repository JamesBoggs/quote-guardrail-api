package online.jamesboggs.guardrail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

/**
 * The JSON body for POST /api/quotes/evaluate. Records are compact immutable
 * classes; Jackson turns incoming JSON into this object automatically.
 */
public record QuoteRequest(
        @Schema(example = "brightline")
        @NotBlank String customerId,

        @NotEmpty @Valid List<Line> lines) {

    public record Line(
            @Schema(example = "SKU-110")
            @NotBlank String sku,

            @Schema(example = "4")
            @Positive int quantity,

            @Schema(example = "12.5", description = "Discount off list price, in percent")
            @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discountPct) {
    }
}
