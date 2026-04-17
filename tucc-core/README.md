# tucc-core

API REST de leitura que expõe os dados sincronizados no **PostgreSQL**. Não realiza nenhuma escrita — é exclusivamente consulta.

![Diagrama de arquitetura](../docs/arch/tucc-sync-db-claude.png)

## Endpoints

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `/produtos` | Lista produtos (paginado) |
| `GET` | `/produtos/{id}` | Busca produto por ID |
| `GET` | `/empresas` | Lista empresas (paginado) |
| `GET` | `/empresas/{id}` | Busca empresa por ID |
| `GET` | `/produtos-empresas` | Lista associações produto-empresa (paginado) |
| `GET` | `/produtos-empresas/{id}` | Busca associação por ID |

Documentação interativa disponível em `/swagger-ui.html` quando a aplicação estiver rodando.

## Variáveis de ambiente

| Variável | Descrição |
|----------|-----------|
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuário do PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | Senha do PostgreSQL |

## Build e execução

```bash
./mvnw clean package
./mvnw spring-boot:run
```

A API sobe na porta `8080` por padrão.
