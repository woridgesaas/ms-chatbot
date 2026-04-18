CREATE TABLE IF NOT EXISTS knowledge_base (
                                              id         BIGSERIAL     PRIMARY KEY,
                                              contenu    TEXT          NOT NULL,
                                              vecteur    vector(384),
    source     VARCHAR(500),
    doc_type   VARCHAR(100),
    tenant     VARCHAR(100)  NOT NULL,
    created_at TIMESTAMP     DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS
    idx_knowledge_base_tenant
    ON knowledge_base(tenant);

CREATE INDEX IF NOT EXISTS
    idx_knowledge_base_vecteur
    ON knowledge_base
    USING ivfflat (vecteur vector_cosine_ops)
    WITH (lists = 100);