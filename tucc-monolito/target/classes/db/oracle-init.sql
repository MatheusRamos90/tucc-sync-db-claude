-- =============================================================================
-- oracle-init.sql
-- Executado como usuário tucc no XEPDB1 pelo container gvenzl/oracle-xe
-- Caminho no container: /container-entrypoint-initdb.d/02-init.sql
-- =============================================================================

-- ========================
-- Tabela PRODUTO
-- ========================
CREATE TABLE produto (
    id             NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome           VARCHAR2(255) NOT NULL,
    valor          NUMBER(10, 2),
    desconto       NUMBER(10, 2),
    dt_criacao     TIMESTAMP     DEFAULT SYSTIMESTAMP,
    dt_atualizacao TIMESTAMP     DEFAULT SYSTIMESTAMP
);

-- ========================
-- Tabela EMPRESA
-- ========================
CREATE TABLE empresa (
    id             NUMBER(10)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome           VARCHAR2(255) NOT NULL,
    cnpj           VARCHAR2(18),
    dt_criacao     TIMESTAMP     DEFAULT SYSTIMESTAMP,
    dt_atualizacao TIMESTAMP     DEFAULT SYSTIMESTAMP
);

-- ========================
-- Tabela PRODUTO_EMPRESA
-- ========================
CREATE TABLE produto_empresa (
    id         NUMBER(10) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto_id NUMBER(10) NOT NULL REFERENCES produto(id),
    empresa_id NUMBER(10) NOT NULL REFERENCES empresa(id)
);

-- ========================
-- Supplemental Logging (CDC Debezium)
-- Necessário para que o LogMiner capture o valor das colunas antes e depois de cada operação
-- ========================
ALTER TABLE produto       ADD SUPPLEMENTAL LOG DATA ALL COLUMNS;
ALTER TABLE empresa       ADD SUPPLEMENTAL LOG DATA ALL COLUMNS;
ALTER TABLE produto_empresa ADD SUPPLEMENTAL LOG DATA ALL COLUMNS;
