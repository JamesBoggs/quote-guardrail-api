package online.jamesboggs.guardrail.repository;

import online.jamesboggs.guardrail.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data writes the implementation at startup. findAll(), findById() and save()
 * come free from JpaRepository; findByProductLine is generated from the method name.
 */
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByProductLineIgnoreCase(String productLine);
}
