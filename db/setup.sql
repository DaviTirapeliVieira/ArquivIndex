-- Setup do banco de dados do ArquivIndex (v2 — modelo com quatro entidades).
-- Execute este script uma vez em uma instância MySQL 8 local antes de rodar o programa.

CREATE DATABASE IF NOT EXISTS arquivindex
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'arquivindex_app'@'localhost' IDENTIFIED BY 'senha123';
GRANT ALL PRIVILEGES ON arquivindex.* TO 'arquivindex_app'@'localhost';
FLUSH PRIVILEGES;

USE arquivindex;

-- COLECAO — pasta lógica que agrupa arquivos (1:N com arquivo)
CREATE TABLE IF NOT EXISTS colecao (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL UNIQUE,
    descricao     VARCHAR(300),
    data_criacao  DATETIME NOT NULL
);

-- CATEGORIA — etiqueta/tag aplicável a vários arquivos (N:N)
CREATE TABLE IF NOT EXISTS categoria (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(60) NOT NULL UNIQUE,
    cor   VARCHAR(7)
);

-- ARQUIVO — entidade principal do catálogo
CREATE TABLE IF NOT EXISTS arquivo (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(150) NOT NULL,
    tipo              VARCHAR(50)  NOT NULL,
    descricao         VARCHAR(500),
    caminho           VARCHAR(400),
    favorito          BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao      DATETIME NOT NULL,
    data_modificacao  DATETIME NOT NULL,
    colecao_id        INT,

    CONSTRAINT fk_arquivo_colecao
        FOREIGN KEY (colecao_id) REFERENCES colecao(id)
        ON DELETE SET NULL
);

-- ARQUIVO_CATEGORIA — tabela de junção do relacionamento N:N
CREATE TABLE IF NOT EXISTS arquivo_categoria (
    arquivo_id    INT NOT NULL,
    categoria_id  INT NOT NULL,

    PRIMARY KEY (arquivo_id, categoria_id),

    CONSTRAINT fk_ac_arquivo
        FOREIGN KEY (arquivo_id) REFERENCES arquivo(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ac_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id)
        ON DELETE CASCADE
);

-- HISTORICO_ALTERACAO — trilha de auditoria (1:N com arquivo)
CREATE TABLE IF NOT EXISTS historico_alteracao (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    arquivo_id      INT NOT NULL,
    campo_alterado  VARCHAR(50)  NOT NULL,
    valor_anterior  VARCHAR(500),
    valor_novo      VARCHAR(500),
    tipo_operacao   VARCHAR(20)  NOT NULL,
    data_alteracao  DATETIME     NOT NULL,

    CONSTRAINT fk_historico_arquivo
        FOREIGN KEY (arquivo_id) REFERENCES arquivo(id)
        ON DELETE CASCADE
);

-- Índices para acelerar buscas e ordenações frequentes
CREATE INDEX idx_arquivo_nome      ON arquivo(nome);
CREATE INDEX idx_arquivo_tipo      ON arquivo(tipo);
CREATE INDEX idx_arquivo_data_mod  ON arquivo(data_modificacao);
CREATE INDEX idx_historico_data    ON historico_alteracao(data_alteracao);
