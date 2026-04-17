CREATE TABLE IF NOT EXISTS produto (
    id         BIGINT         PRIMARY KEY,
    nome       VARCHAR(255)   NOT NULL,
    valor      NUMERIC(10, 2) NOT NULL,
    desconto   NUMERIC(10, 2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
