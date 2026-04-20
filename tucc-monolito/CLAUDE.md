# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Visão Geral

`tucc-monolito` é a **única fonte de escrita no Oracle**. É uma aplicação Java 8 com Servlet/JSP puro (sem framework MVC) e JDBC direto via Apache Commons DBCP. Microsserviços nunca escrevem no Oracle — apenas leem via Debezium CDC.

---

## Build e Execução

```bash
mvn clean package      # gera target/tucc-monolito.war
mvn tomcat7:run        # sobe em http://localhost:8090
```

Não há Maven wrapper — use `mvn` diretamente (Java 8 no PATH).

Não há testes automatizados neste módulo.

---

## Arquitetura Interna

Cada entidade segue a tríade **Servlet → DAO → JSP**:

- **`servlet/`** — `HttpServlet` com `doGet`/`doPost`/`doDelete`, serialização JSON via Jackson, mapeado em `web.xml` sob `/api/<entidade>`
- **`dao/`** — JDBC puro; `ConnectionFactory` cria o `BasicDataSource` a partir de variáveis de ambiente; cada DAO usa try-with-resources
- **`model/`** — POJOs com getters/setters
- **`webapp/<entidade>/lista.jsp` e `form.jsp`** — Bootstrap 3 + jQuery; lista.jsp faz AJAX para `/api/<entidade>`, form.jsp faz POST JSON para o mesmo endpoint

O `web.xml` registra os três servlets: `/api/produto`, `/api/empresa`, `/api/produto-empresa`.

---

## Padrão DAO

```java
// inserir — Oracle gera o ID via GENERATED ALWAYS AS IDENTITY; nunca passe o ID no INSERT
"INSERT INTO produto (nome, valor, desconto, dt_criacao, dt_atualizacao) VALUES (?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP)"

// atualizar — sempre atualiza dt_atualizacao
"UPDATE produto SET nome = ?, valor = ?, desconto = ?, dt_atualizacao = SYSTIMESTAMP WHERE id = ?"
```

`BigDecimal` nullable: use `stmt.setNull(idx, Types.NUMERIC)` quando o valor for `null`.

---

## Variáveis de Ambiente

| Variável | Padrão |
|----------|--------|
| `ORACLE_HOST` | `localhost` |
| `ORACLE_PORT` | `1521` |
| `ORACLE_DB` | `XEPDB1` |
| `ORACLE_USER` | `tucc` |
| `ORACLE_PASSWORD` | `tucc` |

---

## Armadilhas

- **`BigDecimal` no JSON:** o Servlet recebe JSON, então `.` é sempre separador decimal — sem problema de locale. O `BigDecimalConverter` do Struts 2 mencionado no CLAUDE.md raiz **não existe mais** neste módulo (sem Struts 2).
- **IDs nunca inseridos pelo Java:** Oracle usa `GENERATED ALWAYS AS IDENTITY`; tentar passar `id` no `INSERT` lança `ORA-32795`.
- **Exclusão com FK:** excluir um `produto` vinculado a `produto_empresa` lança `RuntimeException` no DAO — o Servlet retorna HTTP 409.

---

## Ao Adicionar Nova Entidade

1. Criar `model/NomeEntidade.java`
2. Criar `dao/NomeEntidadeDAO.java` seguindo o padrão de try-with-resources
3. Criar `servlet/NomeEntidadeServlet.java` com `doGet`/`doPost`/`doDelete`
4. Registrar servlet em `web.xml`
5. Criar `webapp/nome_entidade/lista.jsp` e `form.jsp`
6. Adicionar tabela Oracle em `resources/db/oracle-init.sql`
7. Atualizar `CLAUDE.md` raiz e `docs/prompts/system-prompt.md` com o mapeamento Oracle → PostgreSQL