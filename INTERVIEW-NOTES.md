# Interview notes: walking through this project

Keep this file out of the public repo if you prefer (add it to `.gitignore`), or keep it; it doesn't hurt.

## The 30-second version

"To get up to speed on Java for this role, I rebuilt the approval step from my pricing tool as a Spring Boot service. You post a quote, it prices each line, checks margin floors and tier discount caps, and returns approve, route to VP or block with the reasons. Every decision is saved for an audit trail. It uses Spring Data JPA on an in-memory H2 database, and it has unit tests on the rules plus MockMvc tests on the endpoints. My production work is Python and SQL, so this was about learning how the same ideas look in Java."

## Questions you're likely to get, answered from your code

**What does `@RestController` do? How is it different from `@Controller`?**
`@RestController` is `@Controller` plus `@ResponseBody`: whatever the method returns is written to the response body as JSON. A plain `@Controller` returns a view name or a redirect, like `HomeController`, which redirects `/` to Swagger.

**How does an object become JSON?**
Spring Boot includes Jackson. It serializes return values automatically (records use their component names as keys), and `@RequestBody` turns incoming JSON into a `QuoteRequest`.

**`@GetMapping` vs `@PostMapping`?**
GET reads and shouldn't change anything (`/api/products`). POST sends data and can create something; `/api/quotes/evaluate` saves a decision, which is why it returns 201 Created.

**`@PathVariable` vs `@RequestParam`?**
Path variable is part of the URL path (`/api/products/SKU-110`); request param is the query string (`/api/products?line=Climate`).

**How do you handle errors?**
The service throws `NotFoundException`; `ApiExceptionHandler` (`@RestControllerAdvice`) catches it once for the whole app and returns a 404 in the standard ProblemDetail format. Validation failures from `@Valid` become a 400 that lists the bad fields.

**What's dependency injection? Why constructor injection?**
Spring creates the objects (beans) and passes each class what it needs. `GuardrailService` gets its repositories and policy through its constructor. That makes dependencies explicit, lets the fields be `final`, and makes it easy to pass test versions.

**What is Spring Data JPA doing?**
`ProductRepository extends JpaRepository<Product, String>` gives `findAll`, `findById` and `save` with no implementation. `findByProductLineIgnoreCase` is a derived query: Spring reads the method name and writes the SQL. `@Entity` maps a class to a table.

**JPA vs JDBC?**
JDBC is the low-level API: you write the SQL and map result rows yourself. JPA/Hibernate maps objects to tables and writes most SQL for you. For a complex report I'd still drop to SQL (`@Query` or `JdbcTemplate`).

**Why `BigDecimal` and not `double`?**
`double` is binary floating point, so 0.1 + 0.2 isn't exactly 0.3. For money you need exact decimal math and a chosen rounding mode (`RoundingMode.HALF_UP`). Compare with `compareTo`, not `equals`, because `equals` also checks scale (2.0 vs 2.00).

**Where do you use streams?**
`GuardrailRules` maps inputs to priced lines with `.map(...).toList()` and totals revenue with `.map(PricedLine::revenue).reduce(BigDecimal.ZERO, BigDecimal::add)`. `GuardrailService` builds a SKU lookup with `Collectors.toMap`. Python equivalent: list comprehensions and `sum()`.

**Why is `GuardrailRules` plain Java?**
Separation of concerns: the business logic has no web or database code, so it's fast to unit test and easy to read. Controllers handle HTTP, the service handles data access, the rules handle pricing.

**What's a record?**
A compact, immutable data class (Java 16+). The compiler writes the constructor, accessors, `equals`, `hashCode` and `toString`. The DTOs and the policy are records.

**`@Transactional`?**
Runs the method in one database transaction: if something fails partway, the changes roll back. `readOnly = true` on read methods is a hint that lets Hibernate skip some work.

**What would you add for production?**
A real database (Postgres) with migrations (Flyway), authentication, pagination on `/api/quotes`, and storing the line items of each quote, not just totals.

## Core Java to review alongside

- **Collections:** `List` (ordered, duplicates: `ArrayList`), `Set` (unique: `HashSet`), `Map` (key to value: `HashMap` unordered, `TreeMap` sorted by key, `LinkedHashMap` keeps insertion order).
- **Interfaces vs abstract classes:** an interface is a contract (a class can implement many); an abstract class can hold shared state and code (a class extends only one).
- **Exceptions:** checked (must be declared or caught, like `IOException`) vs unchecked (`RuntimeException`, like `NotFoundException` here).
- **`equals` and `hashCode`:** if you override one, override both, or objects break as `HashMap` keys.
- **`final`, `static`, access modifiers** (`private`, `protected`, `public`, package-private).
