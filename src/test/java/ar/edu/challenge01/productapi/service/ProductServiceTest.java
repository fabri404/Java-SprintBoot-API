package ar.edu.challenge01.productapi.service;

import ar.edu.challenge01.productapi.dto.ProductRequest;
import ar.edu.challenge01.productapi.dto.ProductResponse;
import ar.edu.challenge01.productapi.entity.Product;
import ar.edu.challenge01.productapi.repository.ProductRepository;
import ar.edu.challenge01.productapi.web.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  private ProductRepository repo;

  private ProductService service;

  @BeforeEach
  void setUp() {
    // ajustá el ctor si tu ProductService recibe más cosas
    service = new ProductService(repo);
  }

  @Test
  void create_guarda_y_devuelveResponse() {
    // 1) lo que llega al service (DTO)
    ProductRequest req = new ProductRequest(
        "Mouse",
        "Inalámbrico",
        new BigDecimal("19999.90")
    );

    // 2) lo que el repo “guardaría” (entity)
    Product saved = new Product();
    saved.setId(10L);
    saved.setName(req.name());
    saved.setDescription(req.description());
    saved.setPrice(req.price());
    saved.setCreatedAt(Instant.now());

    when(repo.save(any(Product.class))).thenReturn(saved);

    // 3) ejecuto
    ProductResponse res = service.create(req);

    // 4) verifico
    assertThat(res.id()).isEqualTo(10L);
    assertThat(res.name()).isEqualTo("Mouse");

    // y de paso verifico que realmente se llamó al repo con los datos correctos
    ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
    verify(repo).save(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo("Mouse");
    assertThat(captor.getValue().getDescription()).isEqualTo("Inalámbrico");
  }

  @Test
  void findById_existente_devuelveResponse() {
    Product db = new Product();
    db.setId(1L);
    db.setName("Teclado");
    db.setDescription("Mecánico");
    db.setPrice(new BigDecimal("1500"));
    db.setCreatedAt(Instant.now());

    when(repo.findById(1L)).thenReturn(Optional.of(db));

    ProductResponse res = service.findById(1L);

    assertThat(res.id()).isEqualTo(1L);
    assertThat(res.name()).isEqualTo("Teclado");
  }

  @Test
  void findById_inexistente_lanzaNotFound() {
    when(repo.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(99L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  void findAll_devuelveListaDeResponses() {
    Product p1 = new Product();
    p1.setId(1L); p1.setName("A"); p1.setDescription("d1");
    p1.setPrice(new BigDecimal("10")); p1.setCreatedAt(Instant.now());

    Product p2 = new Product();
    p2.setId(2L); p2.setName("B"); p2.setDescription("d2");
    p2.setPrice(new BigDecimal("20")); p2.setCreatedAt(Instant.now());

    when(repo.findAll()).thenReturn(List.of(p1, p2));

    var list = service.findAll();

    assertThat(list).hasSize(2);
    assertThat(list.get(0).name()).isEqualTo("A");
  }
}

