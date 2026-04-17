---
name: db-sync
description: Guia de mapeamento de tipos e sincronização entre Oracle e PostgreSQL. Use ao criar ou revisar entidades JPA, mapeamentos de tipo, ou ao adicionar novas tabelas ao processo de sincronização.
allowed-tools: Read, Grep, Glob, Edit
---

# Skill: db-sync — Mapeamento Oracle → PostgreSQL

## Mapeamento de Tipos por Tabela

### PRODUTO
| Campo Oracle          | Tipo Oracle      | Campo Postgres        | Tipo Postgres     |
|-----------------------|------------------|-----------------------|-------------------|
| ID                    | NUMBER(10) IDENTITY | id                 | BIGINT (PK)       |
| NOME                  | VARCHAR2(255)    | nome                  | VARCHAR(255)      |
| VALOR                 | NUMBER(10,2)     | valor                 | NUMERIC(10,2)     |
| DESCONTO              | NUMBER(10,2)     | desconto              | NUMERIC(10,2)     |
| DT_CRIACAO            | TIMESTAMP        | created_at            | TIMESTAMP         |
| DT_ATUALIZACAO        | TIMESTAMP        | updated_at            | TIMESTAMP         |

### EMPRESA
| Campo Oracle          | Tipo Oracle      | Campo Postgres        | Tipo Postgres     |
|-----------------------|------------------|-----------------------|-------------------|
| ID                    | NUMBER(10) IDENTITY | id                 | BIGINT (PK)       |
| NOME                  | VARCHAR2(255)    | nome                  | VARCHAR(255)      |
| CNPJ                  | VARCHAR2(18)     | cnpj                  | VARCHAR(18)       |
| DT_CRIACAO            | TIMESTAMP        | created_at            | TIMESTAMP         |
| DT_ATUALIZACAO        | TIMESTAMP        | updated_at            | TIMESTAMP         |

### PRODUTO_EMPRESA
| Campo Oracle          | Tipo Oracle      | Campo Postgres        | Tipo Postgres     |
|-----------------------|------------------|-----------------------|-------------------|
| ID                    | NUMERIC(10)      | id                    | BIGINT (PK)       |
| PRODUTO_ID            | NUMERIC(10)      | produto_id            | BIGINT (FK)       |
| EMPRESA_ID            | NUMERIC(10)      | empresa_id            | BIGINT (FK)       |

> PRODUTO_EMPRESA não possui campos de auditoria no Oracle — não adicione `created_at`/`updated_at` nesta entidade.

## Regras de Mapeamento

1. **IDs do Oracle são as chaves naturais no Postgres** — não gere IDs locais
2. **Preserve timestamps exatamente** como vieram do Oracle — não use `LocalDateTime.now()`
3. **CNPJ**: armazene como string, sem formatação (apenas dígitos ou com pontuação — decida e documente)
4. **Valores monetários**: NUMERIC(10,2) no Postgres — use `BigDecimal` no Java, nunca `double`

## Formato dos Valores no Kafka (Debezium Oracle)

O conector Oracle usa `decimal.handling.mode=string` — **todos os campos numéricos chegam como `String` no payload Kafka**, não como `Number`. Os helpers de conversão no `SyncService` devem tratar ambos:

```java
private Long toLong(Object value) {
    if (value instanceof Number n) return n.longValue();
    return Long.parseLong(value.toString().trim());
}

private BigDecimal toBigDecimal(Object value) {
    return new BigDecimal(value.toString().trim());
}
```

Campos `TIMESTAMP` do Oracle chegam como **epoch microssegundos** (não milissegundos):

```java
private Instant toInstant(Object value) {
    long micros = value instanceof Number n ? n.longValue() : Long.parseLong(value.toString().trim());
    return Instant.ofEpochSecond(0, micros * 1000L); // micros → nanos
}
```

## Convenção de Campos de Auditoria (Datas)

Campos de data de criação e atualização **devem seguir o padrão `created_at` / `updated_at`** no Postgres e na entidade JPA, independentemente do nome original no Oracle (`DT_CRIACAO`, `DATA_INCLUSAO`, `CREATED`, etc.).

**Regra:** só mapeie esses campos se eles existirem na tabela Oracle de origem. Se o legado não os tiver, não os crie na entidade — nunca preencha com `Instant.now()` para simular auditoria inexistente.

```java
// ✅ Tabela Oracle TEM os campos de data
@Column(name = "created_at")
private Instant createdAt;   // mapeado de DT_CRIACAO

@Column(name = "updated_at")
private Instant updatedAt;   // mapeado de DT_ATUALIZACAO

// ✅ Tabela Oracle NÃO tem campos de data → simplesmente omita os campos na entidade
```

## Relacionamentos JPA — LAZY Loading

A entidade `ProdutoEmpresaEntity` guarda apenas as FKs (`produto_id`, `empresa_id`). Para navegar até os dados completos de `Produto` ou `Empresa`, use `@ManyToOne(fetch = FetchType.LAZY)`.

**Regra:** relacionamentos entre entidades sincronizadas do Oracle devem ser **sempre LAZY** — nunca EAGER. O carregamento dos dados relacionados só deve ocorrer quando explicitamente necessário (ex: dentro de uma `@Transactional` no `tucc-core`).

```java
@Entity
@Table(name = "produto_empresa")
public class ProdutoEmpresaEntity {

    @Id
    private Long id;

    // FK pura — para queries simples e sincronização
    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "empresa_id")
    private Long empresaId;

    // Relacionamento navegável — carregado sob demanda (LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", insertable = false, updatable = false)
    private ProdutoEntity produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", insertable = false, updatable = false)
    private EmpresaEntity empresa;
}
```

> `insertable = false, updatable = false` nos relacionamentos LAZY é obrigatório porque o controle da FK é feito pelos campos `produtoId` / `empresaId` — não pelo objeto navegável. Isso evita conflito de mapeamento duplo da mesma coluna.

## Checklist ao Adicionar Nova Tabela

- [ ] Adicionar tabela ao conector Debezium (`table.include.list`)
- [ ] Criar `record` de payload no producer
- [ ] Criar entidade JPA no consumer
- [ ] Verificar se a tabela Oracle possui campos de data → mapear como `created_at`/`updated_at` apenas se existirem
- [ ] Se a tabela tem FK para outra entidade → adicionar `@ManyToOne(fetch = LAZY)` com `insertable = false, updatable = false`
- [ ] Criar repositório Spring JPA com método `upsert`
- [ ] Criar handler no `SyncService` para a nova tabela
- [ ] Adicionar testes de mapeamento

## Exemplo de Entidade JPA Completa

```java
@Entity
@Table(name = "produto")
public class ProdutoEntity {

    @Id
    private Long id; // ID vem do Oracle — não use @GeneratedValue

    private String nome;
    private BigDecimal valor;
    private BigDecimal desconto;

    // Só presentes porque Oracle tem DT_CRIACAO e DT_ATUALIZACAO
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

## Testes de Integração (Testcontainers)

Verificam que o mapeamento JPA corresponde ao schema real do PostgreSQL.

> **Spring Boot 4:** `@DataJpaTest`, `@AutoConfigureTestDatabase` e `@ImportAutoConfiguration` foram removidos. Use `@SpringBootTest(webEnvironment=NONE)` + `@Transactional`. Flyway autoconfiguration também não existe em Spring Boot 4 — use `ddl-auto=create-drop` em todos os módulos.

### tucc-sync-db-consumer (entidades com `@Setter`)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
@Transactional
class ProdutoRepositoryIT {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired ProdutoRepository produtoRepository;

    @Test
    void save_devePersistir() {
        ProdutoEntity e = new ProdutoEntity();
        e.setId(1L); e.setNome("A"); e.setValor(new BigDecimal("99.99"));
        produtoRepository.save(e);

        assertThat(produtoRepository.findById(1L).get().getNome()).isEqualTo("A");
    }
}
```

- `@Transactional` garante isolamento entre testes (rollback automático)
- `ddl-auto=create-drop`: Hibernate cria o schema a partir das entidades
- `ProdutoEmpresaRepositoryIT`: salvar Produto + Empresa no `@BeforeEach` (constraints FK)

### tucc-core (entidades `@Getter` only — sem setters)

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
    @Autowired JdbcTemplate jdbcTemplate;

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

- Como não há `@Setter`, usa `JdbcTemplate` para inserir dados de teste
- `@Transactional` garante rollback entre testes → `@BeforeEach` é seguro mesmo com múltiplos testes
- `ddl-auto=create-drop`: Hibernate cria schema a partir das entidades (sem Flyway no tucc-core)
