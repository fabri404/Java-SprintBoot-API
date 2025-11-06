package ar.edu.challenge01.productapi.web;

import ar.edu.challenge01.productapi.entity.Product;
import ar.edu.challenge01.productapi.repository.ProductRepository;
import ar.edu.challenge01.productapi.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Ajustá si tu controller está en otro paquete o se llama distinto:
@WebMvcTest(ProductController.class)
// Importa el Service REAL (el de tu main code, anotado con @Service)
@Import(ProductService.class)
class ProductControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  // Moqueamos SÓLO el repository, para que el Service real funcione con estos datos
  @MockBean ProductRepository repo;

  @Test
  void list_shouldReturn200AndJsonArray() throws Exception {
    Product p = new Product();
    p.setId(1L);
    p.setName("Teclado");
    p.setDescription("Mecánico");
    p.setPrice(new BigDecimal("1500.00"));
    p.setCreatedAt(Instant.now());

    when(repo.findAll()).thenReturn(List.of(p));

    mvc.perform(get("/products"))
       .andExpect(status().isOk())
       .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
       .andExpect(jsonPath("$[0].id").value(1))
       .andExpect(jsonPath("$[0].name").value("Teclado"));
  }

  @Test
  void getOne_shouldReturn200() throws Exception {
    Product p = new Product();
    p.setId(1L);
    p.setName("Teclado");
    p.setDescription("Mecánico");
    p.setPrice(new BigDecimal("1500.00"));
    p.setCreatedAt(Instant.now());

    when(repo.findById(1L)).thenReturn(Optional.of(p));

    mvc.perform(get("/products/1"))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.id").value(1))
       .andExpect(jsonPath("$.name").value("Teclado"));
  }

  @Test
  void create_shouldReturn201() throws Exception {
    Product toCreate = new Product();
    toCreate.setName("Mouse");
    toCreate.setDescription("Inalámbrico");
    toCreate.setPrice(new BigDecimal("19999.90"));

    Product created = new Product();
    created.setId(10L);
    created.setName("Mouse");
    created.setDescription("Inalámbrico");
    created.setPrice(new BigDecimal("19999.90"));
    created.setCreatedAt(Instant.now());

    when(repo.save(any(Product.class))).thenReturn(created);

    mvc.perform(post("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(toCreate)))
       .andExpect(status().isCreated())
       .andExpect(header().exists("Location"))
       .andExpect(jsonPath("$.id").value(10))
       .andExpect(jsonPath("$.name").value("Mouse"));
  }
}

