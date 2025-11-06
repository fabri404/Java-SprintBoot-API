package ar.edu.challenge01.productapi.web;

import ar.edu.challenge01.productapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(ApiExceptionHandler.class)
class ProductValidationTest {

  @Autowired
  private MockMvc mvc;

  // Mockeamos el repo porque el controller lo inyecta
  @MockBean
  private ProductRepository repo;

  @Test
  void create_rechaza_payload_invalido_con_400() throws Exception {
    // Falta name y price => debe fallar Bean Validation
    String body = """
      { "description": "sin name ni price" }
      """;

    mvc.perform(post("/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isBadRequest())
      // Tu handler hoy devuelve "Bad Request" (reason phrase estándar)
      .andExpect(jsonPath("$.error").value("Bad Request"))
      .andExpect(jsonPath("$.message").value("Validation failed"))
      .andExpect(jsonPath("$.details.name").value("must not be blank"));
  }
}

