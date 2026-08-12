package com.atlaspay.app.testinfra;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for all Spring Boot integration tests in AtlasPay.
 *
 * <p>Starts a single shared MySQL Testcontainer (reused across the entire test suite via
 * {@code @Testcontainers(disabledWithoutDocker = true)} + static container field) and wires its
 * JDBC URL, username and password into the Spring {@code DataSource} via
 * {@link DynamicPropertySource}.
 *
 * <p>Extend this class in any module integration test that needs a real database:
 * <pre>{@code
 * class RegisterMerchantIntegrationTest extends BaseIntegrationTest {
 *     // test methods here
 * }
 * }</pre>
 *
 * <p>WireMock for external HTTP stubs is intentionally <em>not</em> included here — add it per
 * test class using {@code @WireMockTest} or the {@code WireMockExtension} to keep container
 * lifecycle scoped to the specific adapter under test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    /**
     * Singleton MySQL container shared across the entire test run.
     * Testcontainers reuses the same container instance for all subclasses.
     */
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("atlaspay_test")
            .withUsername("atlaspay_test")
            .withPassword("atlaspay_test")
            .withReuse(true);

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        // Disable Flyway auto-migration in tests; let each test control schema state
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
