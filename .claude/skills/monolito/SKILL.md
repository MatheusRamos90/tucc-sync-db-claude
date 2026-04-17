---
name: monolito
description: Guia para implementar ou modificar o tucc-monolito (Java 8 / Struts 2 / Oracle). Use quando adicionar telas, campos ou tabelas no legado — inclui o impacto em cascata nos microsserviços.
allowed-tools: Read, Grep, Glob, Edit, Bash
---

# Skill: tucc-monolito

## Responsabilidade
CRUD legado em Struts 2 sobre Oracle. É a **única** fonte de escrita no banco Oracle — os microsserviços nunca escrevem nele.

## Estrutura de um CRUD

```
tucc-monolito/src/main/java/.../
  model/EntidadeX.java          ← POJO com getters/setters
  dao/EntidadeXDAO.java         ← JDBC puro (listar, buscarPorId, inserir, atualizar, excluir)
  action/EntidadeXAction.java   ← Struts 2 Action (implements ActionSupport, SessionAware)

tucc-monolito/src/main/webapp/WEB-INF/jsp/entidade_x/
  lista.jsp                     ← tabela com listagem
  form.jsp                      ← formulário de cadastro/edição

tucc-monolito/src/main/resources/
  struts.xml                    ← mapeamento de actions e results
  xwork-conversion.properties   ← type converters globais (BigDecimalConverter já registrado)

tucc-monolito/src/main/resources/db/
  oracle-init.sql               ← DDL do Oracle (atualizar ao adicionar tabela/coluna)
```

## Padrão de Implementação

### 1. Model
```java
public class EntidadeX {
    private Long id;
    private String nome;
    private BigDecimal valor;   // NUNCA use double para monetários
    private Date dtCriacao;
    // getters e setters
}
```

### 2. DAO
```java
public class EntidadeXDAO {
    public void inserir(EntidadeX e) {
        // INSERT sem o campo `id` — Oracle usa GENERATED ALWAYS AS IDENTITY
        String sql = "INSERT INTO entidade_x (nome, valor, dt_criacao) VALUES (?, ?, SYSTIMESTAMP)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getNome());
            stmt.setBigDecimal(2, e.getValor());  // usa setBigDecimal, nunca setDouble
            stmt.executeUpdate();
        }
    }
    // atualizar, excluir, listar, buscarPorId seguem o mesmo padrão
}
```

### 3. Action
```java
public class EntidadeXAction extends ActionSupport implements SessionAware {
    private EntidadeX entidadeX = new EntidadeX();
    private List<EntidadeX> lista;
    private Long id;
    private Map<String, Object> session;

    public String salvar() {
        if (entidadeX.getId() == null) dao.inserir(entidadeX);
        else dao.atualizar(entidadeX);
        session.put("flashMessage", "Salvo com sucesso!");
        session.put("flashType", "success");
        return SUCCESS; // redireciona para listar via struts.xml
    }
    // listar(), novo(), editar(), excluir()...
    @Override public void setSession(Map<String, Object> session) { this.session = session; }
}
```

### 4. struts.xml — adicionar package e actions
```xml
<package name="entidade-x" namespace="/entidade-x" extends="default">
    <action name="listar"  class="...EntidadeXAction" method="listar">
        <result>/WEB-INF/jsp/entidade_x/lista.jsp</result>
    </action>
    <action name="novo"    class="...EntidadeXAction" method="novo">
        <result>/WEB-INF/jsp/entidade_x/form.jsp</result>
    </action>
    <action name="editar"  class="...EntidadeXAction" method="editar">
        <result>/WEB-INF/jsp/entidade_x/form.jsp</result>
        <result name="error">/WEB-INF/jsp/entidade_x/lista.jsp</result>
    </action>
    <action name="salvar"  class="...EntidadeXAction" method="salvar">
        <result type="redirectAction">listar</result>
        <result name="input">/WEB-INF/jsp/entidade_x/form.jsp</result>
    </action>
    <action name="excluir" class="...EntidadeXAction" method="excluir">
        <result type="redirectAction">listar</result>
    </action>
</package>
```

---

## Type Conversion — Armadilhas

### BigDecimal em formulários (CRÍTICO)
O JVM pode rodar com locale `pt_BR`. Nesse locale, `.` é separador de milhar, então Struts 2 converte `5200.97` para `520097`. O projeto já tem `BigDecimalConverter` registrado em `xwork-conversion.properties` que resolve isso. **Ao adicionar novos campos `BigDecimal`, nenhuma ação extra é necessária** — o converter já é global.

### Datas em formulários
Use `String` no model e parse manualmente no DAO com `Timestamp.valueOf(LocalDateTime.parse(...))` caso precise aceitar datas via form. Para datas geradas pelo Oracle (`SYSTIMESTAMP`), não exponha o campo no form.

---

## Impacto nos Microsserviços

### Quando uma mudança no monolito REQUER mudança nos microsserviços

| Mudança no monolito | O que muda nos microsserviços |
|---------------------|-------------------------------|
| Nova tabela Oracle | Pipeline completo — use `/add-table NOME` |
| Novo campo em tabela existente | Flyway migration + entidade consumer + DTO core |
| Campo renomeado | Flyway migration (renomear coluna) + entidade consumer + DTO core |
| Campo removido | Flyway migration (DROP COLUMN) + entidade consumer + DTO core |
| Nova FK entre tabelas já sincronizadas | `@ManyToOne(fetch=LAZY)` na entidade consumer + migration |

### Quando NÃO há impacto nos microsserviços

- Mudanças somente em JSP/HTML/CSS
- Novo campo que não deve ser sincronizado (ex: campo de sessão, controle interno)
- Alterações em lógica de negócio que não mudam a estrutura da tabela Oracle

---

## Checklist: Novo Campo em Tabela Existente

- [ ] `oracle-init.sql`: adicionar `ALTER TABLE ... ADD coluna TIPO` (para novos deploys)
- [ ] `Model`: adicionar getter/setter
- [ ] `DAO.inserir()`: adicionar parâmetro no SQL e `stmt.set*()`
- [ ] `DAO.atualizar()`: idem
- [ ] `DAO.mapRow()`: adicionar leitura do `ResultSet`
- [ ] JSP `form.jsp`: adicionar `<input name="entidade.campo"/>`
- [ ] `SyncService` (consumer): adicionar campo no mapper `toEntidadeEntity()`
- [ ] Flyway migration (consumer): `ALTER TABLE ... ADD COLUMN` no PostgreSQL
- [ ] Entidade JPA (consumer): novo campo com `@Column`
- [ ] DTO de resposta (core): novo campo no record `EntidadeResponse`

## Checklist: Nova Tabela (CRUD completo + sincronização)

- [ ] `oracle-init.sql`: `CREATE TABLE` + `GENERATED ALWAYS AS IDENTITY` + `SUPPLEMENTAL LOG DATA ALL COLUMNS`
- [ ] Model + DAO + Action + JSPs
- [ ] `struts.xml`: registrar package
- [ ] Executar `/add-table NOME_TABELA` para a camada de sincronização

---

## Oracle DDL — Convenções

```sql
-- IDs: sempre IDENTITY, nunca inseridos pelo Java
id NUMBER(10) GENERATED ALWAYS AS IDENTITY PRIMARY KEY

-- Numéricos monetários
valor NUMBER(10, 2)

-- Datas de auditoria
dt_criacao     TIMESTAMP DEFAULT SYSTIMESTAMP
dt_atualizacao TIMESTAMP DEFAULT SYSTIMESTAMP

-- Supplemental logging: obrigatório para CDC com Debezium
ALTER TABLE nova_tabela ADD SUPPLEMENTAL LOG DATA ALL COLUMNS;
```
