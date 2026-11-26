package com.healthdekho.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthdekho.domain.Customer;
import com.healthdekho.domain.CustomerPolicy;
import com.healthdekho.domain.InsuranceProduct;
import com.healthdekho.domain.PolicyStatus;
import com.healthdekho.domain.PolicyType;
import com.healthdekho.domain.PremiumFrequency;
import com.healthdekho.domain.PremiumInstallment;
import com.healthdekho.dto.AuthDtos;
import com.healthdekho.dto.DashboardDtos;
import com.healthdekho.dto.PolicyDtos;
import com.healthdekho.dto.ProductDtos;
import com.healthdekho.dto.PremiumDtos;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.repository.InsuranceProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.model.bedrock.converse.autoconfigure.BedrockConverseProxyChatProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:dashboard;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false"
})
class DashboardIntegrationTest {
    @Autowired private TestRestTemplate http;
    @Autowired private CustomerRepository customers;
    @Autowired private InsuranceProductRepository products;
    @Autowired private CustomerPolicyRepository policies;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private BedrockConverseProxyChatProperties bedrockChatProperties;
    @Value("${spring.ai.bedrock.converse.chat.options.model}") private String model;
    @Value("${spring.ai.bedrock.aws.region}") private String region;
    @Value("${app.ai.model-name}") private String recordedModel;

    private Customer customer;
    private InsuranceProduct registered;
    private InsuranceProduct available;
    private HttpEntity<Void> authenticated;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        customer = customers.save(new Customer(suffix + "@example.com", passwordEncoder.encode("test-password"),
                "Dashboard Customer", LocalDate.now().minusYears(30), null));
        registered = products.save(product("REG-" + suffix, "Registered Cover"));
        available = products.save(product("AVAILABLE-" + suffix, "Available Cover"));
        CustomerPolicy policy = new CustomerPolicy("POL-" + suffix, customer, registered, LocalDate.now(),
                LocalDate.now().plusYears(1), PremiumFrequency.MONTHLY, new BigDecimal("100.00"),
                new BigDecimal("10000.00"), new BigDecimal("1200.00"), PolicyStatus.ACTIVE);
        policy.addInstallment(new PremiumInstallment(1, LocalDate.now(), LocalDate.now().plusDays(15),
                new BigDecimal("100.00")));
        policies.save(policy);

        var login = http.postForEntity("/auth/login",
                new AuthDtos.LoginRequest(customer.getEmail(), "test-password"), AuthDtos.LoginResponse.class);
        assertEquals(HttpStatus.OK, login.getStatusCode());
        assertNotNull(login.getBody());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getBody().token());
        authenticated = new HttpEntity<>(headers);
    }

    @Test
    void loadsDashboardWithRegisteredPolicyOutsidePersistenceSession() {
        var response = http.exchange("/dashboard", HttpMethod.GET, authenticated, DashboardDtos.DashboardResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var dashboard = response.getBody();
        assertNotNull(dashboard);
        assertEquals(customer.getEmail(), dashboard.customer().email());
        assertEquals(1, dashboard.registeredPolicies().size());
        assertEquals(registered.getProductCode(), dashboard.registeredPolicies().getFirst().productCode());
        assertEquals("Registered Cover", dashboard.registeredPolicies().getFirst().policyName());
        assertEquals(new DashboardDtos.Statistics(1, 1, 1, 0), dashboard.statistics());
        assertTrue(dashboard.availablePolicies().stream().anyMatch(p -> p.productId().equals(available.getId())));
        assertTrue(dashboard.availablePolicies().stream().noneMatch(p -> p.productId().equals(registered.getId())));
    }

    @Test
    void loadsMyPoliciesWithProductDetails() {
        var response = http.exchange("/policies/my", HttpMethod.GET, authenticated, PolicyDtos.PolicyResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(registered.getProductCode(), response.getBody()[0].productCode());
        assertEquals("Registered Cover", response.getBody()[0].policyName());
    }

    @Test
    void loadsAvailableProductsAndExcludesRegisteredProducts() {
        var response = http.exchange("/products/available", HttpMethod.GET, authenticated, ProductDtos.ProductResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        var ids = java.util.Arrays.stream(response.getBody()).map(ProductDtos.ProductResponse::productId).toList();
        assertTrue(ids.contains(available.getId()));
        assertFalse(ids.contains(registered.getId()));
    }

    @Test
    void permitsErrorDispatchWithoutMakingDashboardPublic() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, http.getForEntity("/error", String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, http.getForEntity("/dashboard", String.class).getStatusCode());
    }

    @Test
    void searchesOnlyOwnedPoliciesWithPaginationAndSorting() {
        var second = products.save(product("SECOND-" + UUID.randomUUID().toString().substring(0, 8), "Another Cover"));
        var policy = new CustomerPolicy("AAA-" + UUID.randomUUID(), customer, second, LocalDate.now(),
                LocalDate.now().plusYears(1), PremiumFrequency.MONTHLY, new BigDecimal("100.00"),
                new BigDecimal("10000.00"), new BigDecimal("1200.00"), PolicyStatus.ACTIVE);
        policies.save(policy);
        var response = http.exchange("/policies/search?policyName=cover&page=0&size=1&sort=product.policyName,asc",
                HttpMethod.GET, authenticated, JsonNode.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().get("totalElements").asInt());
        assertEquals(2, response.getBody().get("totalPages").asInt());
        assertEquals("Another Cover", response.getBody().get("content").get(0).get("policyName").asText());

        var byNumber = http.exchange("/policies/search?policyNumber=" + policy.getPolicyNumber(),
                HttpMethod.GET, authenticated, JsonNode.class);
        assertEquals(HttpStatus.OK, byNumber.getStatusCode());
        assertNotNull(byNumber.getBody());
        assertEquals(1, byNumber.getBody().get("totalElements").asInt());
        assertEquals(policy.getPolicyNumber(), byNumber.getBody().get("content").get(0).get("policyNumber").asText());
    }

    @Test
    void recordsLatePenaltyOnlyOnceAcrossPartialPayments() {
        var product = products.save(product("LATE-" + UUID.randomUUID().toString().substring(0, 8), "Late Cover"));
        var policy = new CustomerPolicy("LATE-" + UUID.randomUUID().toString().substring(0, 8), customer, product, LocalDate.now(),
                LocalDate.now().plusYears(1), PremiumFrequency.MONTHLY, new BigDecimal("100.00"),
                new BigDecimal("10000.00"), new BigDecimal("1200.00"), PolicyStatus.ACTIVE);
        policy.addInstallment(new PremiumInstallment(1, LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(1), new BigDecimal("100.00")));
        policy = policies.saveAndFlush(policy);
        var schedule = http.exchange("/installments/policy/" + policy.getId(), HttpMethod.GET,
                authenticated, PremiumDtos.InstallmentResponse[].class);
        assertNotNull(schedule.getBody());
        var installmentId = schedule.getBody()[0].installmentId();
        var first = http.exchange("/payments/pay", HttpMethod.POST,
                new HttpEntity<>(new PremiumDtos.PayRequest(installmentId, new BigDecimal("40.00"), "UPI"), authenticated.getHeaders()),
                PremiumDtos.PaymentResponse.class);
        assertEquals(HttpStatus.OK, first.getStatusCode());
        assertNotNull(first.getBody());
        assertEquals(0, first.getBody().penaltyAmount().compareTo(new BigDecimal("2.00")));
        var second = http.exchange("/payments/pay", HttpMethod.POST,
                new HttpEntity<>(new PremiumDtos.PayRequest(installmentId, new BigDecimal("60.00"), "UPI"), authenticated.getHeaders()),
                PremiumDtos.PaymentResponse.class);
        assertEquals(HttpStatus.OK, second.getStatusCode());
        assertNotNull(second.getBody());
        assertEquals(0, second.getBody().penaltyAmount().compareTo(BigDecimal.ZERO));
        var history = http.exchange("/payments/policy/" + policy.getId(), HttpMethod.GET,
                authenticated, PremiumDtos.PaymentResponse[].class);
        assertNotNull(history.getBody());
        assertEquals(2, history.getBody().length);
        assertEquals(0, java.util.Arrays.stream(history.getBody()).map(PremiumDtos.PaymentResponse::penaltyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(new BigDecimal("2.00")));
    }

    @Test
    void usesConfiguredBedrockModelAndRegion() {
        assertEquals("us-east-1", region);
        assertEquals("us.anthropic.claude-sonnet-4-6", model);
        assertEquals(model, bedrockChatProperties.getOptions().getModel());
        assertEquals(model, recordedModel);
    }

    private InsuranceProduct product(String code, String name) {
        return new InsuranceProduct(code, name, PolicyType.TERM, 12, PremiumFrequency.MONTHLY,
                new BigDecimal("100.00"), new BigDecimal("10000.00"), 18, 60);
    }
}
