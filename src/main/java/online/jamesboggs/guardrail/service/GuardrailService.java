package online.jamesboggs.guardrail.service;

import online.jamesboggs.guardrail.config.GuardrailPolicy;
import online.jamesboggs.guardrail.dto.QuoteRequest;
import online.jamesboggs.guardrail.dto.QuoteResult;
import online.jamesboggs.guardrail.exception.NotFoundException;
import online.jamesboggs.guardrail.model.Customer;
import online.jamesboggs.guardrail.model.Product;
import online.jamesboggs.guardrail.model.QuoteRecord;
import online.jamesboggs.guardrail.repository.CustomerRepository;
import online.jamesboggs.guardrail.repository.ProductRepository;
import online.jamesboggs.guardrail.repository.QuoteRecordRepository;
import online.jamesboggs.guardrail.rules.GuardrailRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Connects the web layer to the rules: looks up the customer and products,
 * runs GuardrailRules, saves the decision, and builds the response.
 * Dependencies arrive through the constructor (constructor injection), so tests
 * can pass in their own versions.
 */
@Service
public class GuardrailService {

    private final ProductRepository products;
    private final CustomerRepository customers;
    private final QuoteRecordRepository quoteRecords;
    private final GuardrailPolicy policy;

    public GuardrailService(ProductRepository products, CustomerRepository customers,
                            QuoteRecordRepository quoteRecords, GuardrailPolicy policy) {
        this.products = products;
        this.customers = customers;
        this.quoteRecords = quoteRecords;
        this.policy = policy;
    }

    @Transactional
    public QuoteResult evaluate(QuoteRequest request) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found: " + request.customerId()));

        // Load every SKU in the quote in one query, keyed by SKU for quick lookup.
        List<String> skus = request.lines().stream().map(QuoteRequest.Line::sku).distinct().toList();
        Map<String, Product> bySku = products.findAllById(skus).stream()
                .collect(Collectors.toMap(Product::getSku, Function.identity()));

        List<GuardrailRules.LineInput> inputs = request.lines().stream()
                .map(line -> {
                    Product p = bySku.get(line.sku());
                    if (p == null) {
                        throw new NotFoundException("Product not found: " + line.sku());
                    }
                    return new GuardrailRules.LineInput(p.getSku(), line.quantity(),
                            p.getListPrice(), p.getUnitCost(), line.discountPct());
                })
                .toList();

        GuardrailRules.Evaluation eval = GuardrailRules.evaluate(inputs,
                policy.capFor(customer.getTier()), policy.marginFloorPct(), policy.hardFloorPct());

        QuoteRecord saved = quoteRecords.save(new QuoteRecord(customer.getId(), eval.revenue(),
                eval.grossMargin(), eval.marginPct(), eval.decision(), String.join(" ", eval.reasons())));

        List<QuoteResult.LineResult> lineResults = eval.lines().stream()
                .map(l -> new QuoteResult.LineResult(l.sku(), bySku.get(l.sku()).getName(), l.quantity(),
                        l.listPrice(), l.discountPct(), l.netPrice(), l.revenue(), l.cost(), l.marginPct()))
                .toList();

        return new QuoteResult(saved.getId(), customer.getId(), customer.getTier().name(), eval.decision(),
                eval.reasons(), eval.revenue(), eval.cost(), eval.grossMargin(), eval.marginPct(), lineResults);
    }

    @Transactional(readOnly = true)
    public List<QuoteRecord> recentDecisions() {
        return quoteRecords.findTop20ByOrderByCreatedAtDescIdDesc();
    }

    @Transactional(readOnly = true)
    public QuoteRecord decision(Long id) {
        return quoteRecords.findById(id)
                .orElseThrow(() -> new NotFoundException("Quote not found: " + id));
    }
}
