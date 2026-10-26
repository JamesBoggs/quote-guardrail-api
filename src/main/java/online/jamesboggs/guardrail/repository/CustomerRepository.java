package online.jamesboggs.guardrail.repository;

import online.jamesboggs.guardrail.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
