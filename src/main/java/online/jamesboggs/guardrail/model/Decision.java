package online.jamesboggs.guardrail.model;

/** The three possible outcomes for a quote. */
public enum Decision {
    /** Margin and discounts are within policy. */
    APPROVE,
    /** Below the margin floor or over the tier's discount cap: needs a VP to sign off. */
    ROUTE_TO_VP,
    /** Below the hard floor, or a line sells below cost: not allowed. */
    BLOCK
}
