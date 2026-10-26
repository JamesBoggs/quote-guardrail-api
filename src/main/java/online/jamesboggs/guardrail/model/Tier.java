package online.jamesboggs.guardrail.model;

/** Customer pricing tier. Each tier has its own discount cap (set in application.yml). */
public enum Tier {
    STANDARD,
    PREFERRED,
    KEY
}
