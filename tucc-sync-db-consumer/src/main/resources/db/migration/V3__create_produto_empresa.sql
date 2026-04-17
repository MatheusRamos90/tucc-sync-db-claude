CREATE TABLE IF NOT EXISTS produto_empresa (
    id         BIGINT PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produto(id),
    empresa_id BIGINT NOT NULL REFERENCES empresa(id)
);
