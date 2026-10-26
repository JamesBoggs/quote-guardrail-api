package online.jamesboggs.guardrail.exception;

/** Thrown when a SKU, customer or quote doesn't exist. Mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
