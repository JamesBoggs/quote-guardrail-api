package online.jamesboggs.guardrail.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Starts the whole app (with the seeded H2 data) and calls the endpoints
 * through MockMvc, the way a real HTTP client would.
 */
@SpringBootTest
@AutoConfigureMockMvc
class QuoteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listsProducts() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void filtersProductsByLine() throws Exception {
        mvc.perform(get("/api/products").param("line", "controls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void approvesAQuoteWithinPolicy() throws Exception {
        String body = """
                {"customerId": "brightline",
                 "lines": [{"sku": "SKU-110", "quantity": 4, "discountPct": 8}]}
                """;

        mvc.perform(post("/api/quotes/evaluate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decision").value("APPROVE"))
                .andExpect(jsonPath("$.marginPct").value(31.3))
                .andExpect(jsonPath("$.lines[0].netPrice").value(625.60));
    }

    @Test
    void routesAnOverCapDiscountToVp() throws Exception {
        String body = """
                {"customerId": "palm-comfort",
                 "lines": [{"sku": "SKU-300", "quantity": 10, "discountPct": 12}]}
                """;

        mvc.perform(post("/api/quotes/evaluate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decision").value("ROUTE_TO_VP"));
    }

    @Test
    void returns404ForUnknownSku() throws Exception {
        String body = """
                {"customerId": "brightline",
                 "lines": [{"sku": "SKU-999", "quantity": 1, "discountPct": 0}]}
                """;

        mvc.perform(post("/api/quotes/evaluate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product not found: SKU-999"));
    }

    @Test
    void returns400ForInvalidBody() throws Exception {
        String body = """
                {"customerId": "", "lines": [{"sku": "SKU-110", "quantity": 0, "discountPct": 8}]}
                """;

        mvc.perform(post("/api/quotes/evaluate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.customerId").exists());
    }

    @Test
    void exposesPolicy() throws Exception {
        mvc.perform(get("/api/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hardFloorPct").value(22.0));
    }
}
