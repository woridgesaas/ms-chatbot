CREATE TABLE IF NOT EXISTS knowledge_base (
                                              id         BIGSERIAL    PRIMARY KEY,
                                              contenu    TEXT         NOT NULL,
                                              source     VARCHAR(500),
    doc_type   VARCHAR(100),
    tenant     VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    DEFAULT NOW()
    );