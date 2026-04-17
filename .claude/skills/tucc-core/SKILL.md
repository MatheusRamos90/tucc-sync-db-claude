---
name: tucc-core
description: Guia para implementar ou modificar o tucc-core. Use quando trabalhar com endpoints REST, DTOs de resposta, paginação ou documentação Swagger da API de leitura.
allowed-tools: Read, Grep, Glob, Edit, Bash
---

# Skill: tucc-core

## Responsabilidade
Expor via REST os dados sincronizados no PostgreSQL e quaisquer funcionalidades novas da aplicação.

## Regra Central sobre Endpoints

| Tipo de recurso | Métodos permitidos |
|---|---|
| Dados sincronizados do Oracle (PRODUTO, EMPRESA, PRODUTO_EMPRESA) | Somente **GET** |
| Funcionalidades novas sem relação com o sync | Todos os métodos HTTP |

## Estrutura de um Endpoint de Leitura

```java
// ✅ Controller — delega para o Service, retorna DTO (nunca a entidade JPA)
@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Consulta de produtos sincronizados")
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    @Operation(summary = "Lista produtos com paginação")
    public Page<ProdutoResponse> listar(Pageable pageable) {
        return produtoService.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca produto por ID")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return produtoService.buscar(id);
    }
}
```

## DTOs de Resposta

Nunca exponha entidades JPA diretamente. Use records Java 21 como DTOs de resposta:

```java
// ✅ DTO de resposta — record imutável
public record ProdutoResponse(
    Long id,
    String nome,
    BigDecimal valor,
    BigDecimal desconto,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProdutoResponse from(ProdutoEntity entity) {
        return new ProdutoResponse(
            entity.getId(),
            entity.getNome(),
            entity.getValor(),
            entity.getDesconto(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
```

## Paginação

- Use `Pageable` como parâmetro nos endpoints de listagem
- Retorne `Page<T>` — nunca `List<T>` em endpoints que podem retornar muitos registros
- Defina um tamanho padrão razoável via `@PageableDefault`:

```java
public Page<ProdutoResponse> listar(
    @PageableDefault(size = 20, sort = "id") Pageable pageable
) { ... }
```

## Relacionamentos e Lazy Loading

Ao buscar dados com relacionamentos (ex: `ProdutoEmpresa` com `Produto` e `Empresa`), acesse os dados LAZY dentro de um contexto `@Transactional`:

```java
// ✅ Service com @Transactional para navegar relacionamentos LAZY
@Transactional(readOnly = true)
public ProdutoEmpresaResponse buscar(Long id) {
    ProdutoEmpresaEntity entity = repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    return ProdutoEmpresaResponse.from(entity); // acessa entity.getProduto() dentro da transação
}
```

## Documentação Swagger

- Anote o controller com `@Tag`
- Anote cada endpoint com `@Operation(summary = "...")`
- Para erros conhecidos, use `@ApiResponse`:

```java
@ApiResponse(responseCode = "404", description = "Recurso não encontrado")
```

## Checklist de Implementação

- [ ] Controller retorna DTO (record), nunca `Entity`
- [ ] Endpoint de listagem usa `Page<T>` com `@PageableDefault`
- [ ] Service com `@Transactional(readOnly = true)` para leituras
- [ ] Documentado com `@Tag` no controller e `@Operation` em cada endpoint
- [ ] Dados sincronizados do Oracle: somente GET

## Mapeamento de Recursos

| Entidade         | Endpoint base        | DTO de resposta          |
|------------------|----------------------|--------------------------|
| ProdutoEntity    | `/produtos`          | ProdutoResponse          |
| EmpresaEntity    | `/empresas`          | EmpresaResponse          |
| ProdEmpresaEntity| `/produtos-empresas` | ProdutoEmpresaResponse   |

## Testes

### Unitário — `*ServiceTest`
`@ExtendWith(MockitoExtension.class)` + `@Mock` para repositórios. Testa `listar()` e `buscar()` sem contexto Spring.

### Componente — `*ControllerComponentTest`
Testa a cadeia completa `Controller → Service → Repository (mock)` via HTTP. Usa H2 para o `JpaTransactionManager` necessário ao `@Transactional(readOnly=true)` do Service.

```java
// import: org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc (Spring Boot 4)
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=",  // limpa exclusões do test yaml
    "spring.datasource.url=jdbc:h2:mem:testcoredb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=none"  // sem schema — repos são mocks
})
@AutoConfigureMockMvc
class ProdutoControllerComponentTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProdutoRepository produtoRepository;
    @MockitoBean EmpresaRepository empresaRepository;        // contexto carrega todos os repos
    @MockitoBean ProdutoEmpresaRepository produtoEmpresaRepository;

    @Test
    void listar_deveRetornarPage() throws Exception {
        ProdutoEntity entity = mock(ProdutoEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getNome()).thenReturn("Produto A");
        when(entity.getValor()).thenReturn(new BigDecimal("99.99"));
        when(produtoRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entity)));

        mockMvc.perform(get("/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].nome").value("Produto A"));
    }

    @Test
    void buscar_quandoNaoEncontrado_deveRetornar404() throws Exception {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/produtos/99")).andExpect(status().isNotFound());
    }
}
```

**Pontos-chave:**
- Entidades tucc-core têm `@Getter` only → use `mock(Entity.class)` com `when().thenReturn()`
- `ddl-auto=none` + H2: nenhum schema é criado, repositórios são mocks (sem SQL real)
- Todos os 3 repos precisam ser `@MockitoBean` (contexto carrega todos os beans)
- `ProdutoEmpresaResponse.from()` navega LAZY → `getProduto().getNome()` → mockar entidades filhas

### Integração (Testcontainers) — `*RepositoryIT`
Verifica que as entidades read-only mapeiam corretamente para o schema real.

> **Spring Boot 4:** `@DataJpaTest` e `@AutoConfigureTestDatabase` foram removidos. Use `@SpringBootTest(webEnvironment=NONE)` + `@Transactional`.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "spring.autoconfigure.exclude=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@Transactional
class ProdutoRepositoryIT {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired ProdutoRepository produtoRepository;
    @Autowired JdbcTemplate jdbcTemplate;  // necessário pois entidades são @Getter only

    @BeforeEach
    void setUp() {
        // Valores numéricos devem ser BigDecimal, não String
        jdbcTemplate.update("INSERT INTO produto (id, nome, valor) VALUES (?, ?, ?)",
            1L, "A", new BigDecimal("99.99"));
    }

    @Test
    void findById_deveRetornar() {
        assertThat(produtoRepository.findById(1L).get().getNome()).isEqualTo("A");
    }
}
```
