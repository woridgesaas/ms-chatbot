CREATE TABLE IF NOT EXISTS stats_conge (
                                           id                  BIGSERIAL PRIMARY KEY,
                                           tenant              VARCHAR(100) NOT NULL,
    mois                DATE NOT NULL,
    nb_en_conge         INT DEFAULT 0,
    nb_absents          INT DEFAULT 0,
    nb_total_employes   INT DEFAULT 0,
    nb_conge_annuel     INT DEFAULT 0,
    nb_conge_maladie    INT DEFAULT 0,
    nb_conge_maternite  INT DEFAULT 0,
    updated_at          TIMESTAMP,
    UNIQUE(tenant, mois)
    );

CREATE INDEX idx_stats_conge_tenant
    ON stats_conge(tenant);