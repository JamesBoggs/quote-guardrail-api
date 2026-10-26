package online.jamesboggs.guardrail.rules;

import online.jamesboggs.guardrail.model.Decision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * The pricing logic, in plain Java with no Spring or database code.
 * Keeping it separate makes it easy to unit test and easy to explain:
 * prices go in, a decision and its reasons come out.
 *
 * Rules, checked in this order:
 *   1. Any line priced below cost            -> BLOCK
 *   2. Quote margin below the hard floor     -> BLOCK
 *   3. Quote margin below the margin floor   -> ROUTE_TO_VP
 *   4. Any line discount above the tier cap  -> ROUTE_TO_VP
 *   Otherwise                                -> APPROVE
 */
public final class GuardrailRules {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private GuardrailRules() {
    }

    /** One line to price: what the rules need to know about it. */
    public record LineInput(String sku, int quantity, BigDecimal listPrice,
                            BigDecimal unitCost, BigDecimal discountPct) {
    }

    /** One priced line. */
    public record PricedLine(String sku, int quantity, BigDecimal listPrice, BigDecimal discountPct,
                             BigDecimal netPrice, BigDecimal revenue, BigDecimal cost,
                             BigDecimal marginPct) {
    }

    /** The full result for a quote. */
    public record Evaluation(Decision decision, List<String> reasons, BigDecimal revenue,
                             BigDecimal cost, BigDecimal grossMargin, BigDecimal marginPct,
                             List<PricedLine> lines) {
    }

    public static Evaluation evaluate(List<LineInput> inputs, BigDecimal discountCapPct,
                                      BigDecimal marginFloorPct, BigDecimal hardFloorPct) {
        // Price each line. A stream maps every input to a priced line.
        List<PricedLine> lines = inputs.stream()
                .map(GuardrailRules::price)
                .toList();

        // Add up revenue and cost across all lines.
        BigDecimal revenue = lines.stream().map(PricedLine::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cost = lines.stream().map(PricedLine::cost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossMargin = revenue.subtract(cost);
        BigDecimal marginPct = percent(grossMargin, revenue);

        List<String> blockReasons = new ArrayList<>();
        List<String> routeReasons = new ArrayList<>();

        for (PricedLine line : lines) {
            if (line.netPrice().compareTo(costPerUnit(line)) < 0) {
                blockReasons.add(line.sku() + " is priced below cost.");
            }
            if (line.discountPct().compareTo(discountCapPct) > 0) {
                routeReasons.add(line.sku() + " discount of " + pct(line.discountPct())
                        + " is above the tier cap of " + pct(discountCapPct) + ".");
            }
        }

        if (marginPct.compareTo(hardFloorPct) < 0) {
            blockReasons.add("Quote margin of " + pct(marginPct)
                    + " is below the hard floor of " + pct(hardFloorPct) + ".");
        } else if (marginPct.compareTo(marginFloorPct) < 0) {
            routeReasons.add("Quote margin of " + pct(marginPct)
                    + " is below the floor of " + pct(marginFloorPct) + ".");
        }

        Decision decision;
        List<String> reasons;
        if (!blockReasons.isEmpty()) {
            decision = Decision.BLOCK;
            reasons = blockReasons;
        } else if (!routeReasons.isEmpty()) {
            decision = Decision.ROUTE_TO_VP;
            reasons = routeReasons;
        } else {
            decision = Decision.APPROVE;
            reasons = List.of("Margin and discounts are within policy.");
        }

        return new Evaluation(decision, List.copyOf(reasons), revenue, cost, grossMargin, marginPct, lines);
    }

    private static PricedLine price(LineInput in) {
        BigDecimal qty = BigDecimal.valueOf(in.quantity());
        BigDecimal factor = BigDecimal.ONE.subtract(in.discountPct().divide(HUNDRED, 6, RoundingMode.HALF_UP));
        BigDecimal netPrice = in.listPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal revenue = netPrice.multiply(qty).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cost = in.unitCost().multiply(qty).setScale(2, RoundingMode.HALF_UP);
        return new PricedLine(in.sku(), in.quantity(), in.listPrice(), in.discountPct(),
                netPrice, revenue, cost, percent(revenue.subtract(cost), revenue));
    }

    private static BigDecimal costPerUnit(PricedLine line) {
        return line.cost().divide(BigDecimal.valueOf(line.quantity()), 2, RoundingMode.HALF_UP);
    }

    /** part / whole as a percent with one decimal. Zero revenue counts as 0%. */
    private static BigDecimal percent(BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return part.multiply(HUNDRED).divide(whole, 1, RoundingMode.HALF_UP);
    }

    private static String pct(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString() + "%";
    }
}
