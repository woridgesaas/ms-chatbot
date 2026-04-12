CREATE TABLE IF NOT EXISTS stats_recrutement (
                                                 id                          BIGSERIAL PRIMARY KEY,
                                                 tenant                      VARCHAR(100) NOT NULL,
    mois                        DATE NOT NULL,
    nb_offres_publiees          INT DEFAULT 0,
    nb_offres_actives           INT DEFAULT 0,
    nb_candidatures             INT DEFAULT 0,
    nb_candidatures_en_attente  INT DEFAULT 0,
    nb_entretiens_planifies     INT DEFAULT 0,
    nb_entretiens_effectues     INT DEFAULT 0,
    updated_at                  TIMESTAMP,
    UNIQUE(tenant, mois)
    );

CREATE INDEX idx_stats_recrutement_tenant
    ON stats_recrutement(tenant);