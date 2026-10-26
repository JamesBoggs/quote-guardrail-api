package online.jamesboggs.guardrail.config;

import online.jamesboggs.guardrail.model.Tier;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Pricing policy, read from the "guardrail" block in application.yml.
 * Keeping thresholds in config means the rules can change without a code change.
 *
 * @param marginFloorPct  quotes below this gross margin % go to a VP
 * @param hardFloorPct    quotes below this gross margin % are blocked
 * @param discountCapPct  the largest line discount each tier may get without VP approval
 */
@ConfigurationProperties(prefix = "guardrail")
public record GuardrailPolicy(
        BigDecimal marginFloorPct,
        BigDecimal hardFloorPct,
        Map<Tier, BigDecimal> discountCapPct) {

    public BigDecimal capFor(Tier tier) {
        return discountCapPct.getOrDefault(tier, BigDecimal.ZERO);
    }
}
