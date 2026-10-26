package online.jamesboggs.guardrail.repository;

import online.jamesboggs.guardrail.model.QuoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRecordRepository extends JpaRepository<QuoteRecord, Long> {

    /** Derived query: SELECT ... ORDER BY created_at DESC LIMIT 20 */
    List<QuoteRecord> findTop20ByOrderByCreatedAtDescIdDesc();
}
