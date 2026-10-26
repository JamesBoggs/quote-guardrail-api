package online.jamesboggs.guardrail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import online.jamesboggs.guardrail.config.GuardrailPolicy;
import online.jamesboggs.guardrail.dto.QuoteRequest;
import online.jamesboggs.guardrail.dto.QuoteResult;
import online.jamesboggs.guardrail.model.QuoteRecord;
import online.jamesboggs.guardrail.service.GuardrailService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Quotes", description = "Evaluate quotes and review past decisions")
public class QuoteController {

    private final GuardrailService service;
    private final GuardrailPolicy policy;

    public QuoteController(GuardrailService service, GuardrailPolicy policy) {
        this.service = service;
        this.policy = policy;
    }

    /**
     * @RequestBody: Jackson reads the JSON body into a QuoteRequest.
     * @Valid: runs the validation annotations on it; failures become a 400.
     * 201 Created, because each evaluation is saved as a new record.
     */
    @PostMapping("/quotes/evaluate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Price a quote and decide: APPROVE, ROUTE_TO_VP or BLOCK")
    public QuoteResult evaluate(@Valid @RequestBody QuoteRequest request) {
        return service.evaluate(request);
    }

    @GetMapping("/quotes")
    @Operation(summary = "The 20 most recent decisions (audit log)")
    public List<QuoteRecord> recent() {
        return service.recentDecisions();
    }

    @GetMapping("/quotes/{id}")
    @Operation(summary = "One past decision by id")
    public QuoteRecord one(@PathVariable Long id) {
        return service.decision(id);
    }

    @GetMapping("/policy")
    @Operation(summary = "The current margin floors and discount caps")
    public GuardrailPolicy policy() {
        return policy;
    }
}
