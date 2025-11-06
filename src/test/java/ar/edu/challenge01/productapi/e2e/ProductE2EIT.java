package ar.edu.challenge01.productapi.e2e;

import ar.edu.challenge01.productapi.ProductApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = ProductApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductE2EIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("productdb")
      .withUsername("postgres")
      .withPassword("postgres");

  @DynamicPropertySource
  static void dbProps(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate"); // usamos Flyway
    r.add("spring.flyway.enabled", () -> "true");
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate http;

  String base() { return "http://localhost:" + port; }

  @Test
  void postValido_creaYLuegoGetDevuelve200() {
    var payload = Map.of(
        "name", "Teclado",
        "description", "Mecánico",
        "price", new BigDecimal("1234.50")
    );

    var postResp = http.postForEntity(URI.create(base()+"/products"), payload, Map.class);
    assertThat(postResp.getStatusCode().value()).isEqualTo(201);
    var id = ((Number)((Map<?,?>)postResp.getBody()).get("id")).longValue();

    var getResp = http.getForEntity(URI.create(base()+"/products/"+id), Map.class);
    assertThat(getResp.getStatusCode().value()).isEqualTo(200);
    assertThat(getResp.getBody()).containsEntry("name", "Teclado");
  }

  @Test
  void postInvalido_sinName_retorna400() {
    var payload = Map.of(
        "name", "", // inválido por @NotBlank
        "description", "X",
        "price", new BigDecimal("10")
    );

    var postResp = http.postForEntity(URI.create(base()+"/products"), payload, Map.class);
    assertThat(postResp.getStatusCode().value()).isEqualTo(400);
    assertThat(postResp.getBody()).containsEntry("error", "BAD_REQUEST");
  }
}

