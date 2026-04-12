CREATE TABLE IF NOT EXISTS snapshot_employe (
                                                user_id             VARCHAR(255) PRIMARY KEY,
    nom                 VARCHAR(255),
    prenom              VARCHAR(255),
    email               VARCHAR(255),
    departement         VARCHAR(255),
    poste               VARCHAR(255),
    salaire_actuel      DECIMAL(10,2),
    mois_salaire        VARCHAR(10),
    a_demissionne       BOOLEAN DEFAULT FALSE,
    date_demission      DATE,
    solde_conge         INT DEFAULT 0,
    tenant              VARCHAR(100) NOT NULL,
    updated_at          TIMESTAMP
    );

CREATE INDEX idx_snapshot_employe_tenant
    ON snapshot_employe(tenant);