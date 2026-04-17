CREATE TABLE IF NOT EXISTS empresa (
    id         BIGINT       PRIMARY KEY,
    nome       VARCHAR(255) NOT NULL,
    cnpj       VARCHAR(18)  NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
