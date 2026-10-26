package online.jamesboggs.guardrail.rules;

import online.jamesboggs.guardrail.model.Decision;
import online.jamesboggs.guardrail.rules.GuardrailRules.Evaluation;
import online.jamesboggs.guardrail.rules.GuardrailRules.LineInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests: no Spring, no database, runs in milliseconds. */
class GuardrailRulesTest {

    private static final BigDecimal FLOOR = new BigDecimal("28.0");
    private static final BigDecimal HARD_FLOOR = new BigDecimal("22.0");

    private static LineInput condensingUnit(String discountPct) {
        return new LineInput("SKU-110", 4, new BigDecimal("680.00"), new BigDecimal("430.00"),
                new BigDecimal(discountPct));
    }

    private static Evaluation evaluate(List<LineInput> lines, String cap) {
        return GuardrailRules.evaluate(lines, new BigDecimal(cap), FLOOR, HARD_FLOOR);
    }

    @Test
    void approvesQuoteWithinPolicy() {
        Evaluation result = evaluate(List.of(condensingUnit("8")), "15");

        assertThat(result.decision()).isEqualTo(Decision.APPROVE);
        assertThat(result.revenue()).isEqualByComparingTo("2502.40");
        assertThat(result.marginPct()).isEqualByComparingTo("31.3");
    }

    @Test
    void routesToVpWhenMarginBelowFloor() {
        Evaluation result = evaluate(List.of(condensingUnit("15")), "15");

        assertThat(result.decision()).isEqualTo(Decision.ROUTE_TO_VP);
        assertThat(result.marginPct()).isEqualByComparingTo("25.6");
        assertThat(result.reasons()).anyMatch(r -> r.contains("below the floor"));
    }

    @Test
    void routesToVpWhenDiscountAboveTierCap() {
        LineInput thermostat = new LineInput("SKU-300", 10, new BigDecimal("58.00"),
                new BigDecimal("34.00"), new BigDecimal("12"));

        Evaluation result = evaluate(List.of(thermostat), "10");

        assertThat(result.decision()).isEqualTo(Decision.ROUTE_TO_VP);
        assertThat(result.reasons()).singleElement().asString().contains("tier cap");
    }

    @Test
    void blocksWhenMarginBelowHardFloor() {
        Evaluation result = evaluate(List.of(condensingUnit("25")), "15");

        assertThat(result.decision()).isEqualTo(Decision.BLOCK);
        assertThat(result.marginPct()).isEqualByComparingTo("15.7");
    }

    @Test
    void blocksAnyLinePricedBelowCostEvenIfQuoteMarginIsHealthy() {
        LineInput coilGuardAt40 = new LineInput("SKU-210", 2, new BigDecimal("92.00"),
                new BigDecimal("58.00"), new BigDecimal("40"));

        Evaluation result = evaluate(List.of(condensingUnit("8"), coilGuardAt40), "20");

        assertThat(result.marginPct()).isGreaterThan(FLOOR);
        assertThat(result.decision()).isEqualTo(Decision.BLOCK);
        assertThat(result.reasons()).containsExactly("SKU-210 is priced below cost.");
    }
}
