-- ── snapshot_employe ──
DROP INDEX IF EXISTS idx_snapshot_employe_tenant;

ALTER TABLE snapshot_employe
DROP COLUMN IF EXISTS tenant;

-- ── stats_conge ──
DROP INDEX IF EXISTS idx_stats_conge_tenant;

ALTER TABLE stats_conge
DROP COLUMN IF EXISTS tenant;

-- ── stats_recrutement ──
DROP INDEX IF EXISTS idx_stats_recrutement_tenant;

ALTER TABLE stats_recrutement
DROP COLUMN IF EXISTS tenant;

-- ── knowledge_base ──
DROP INDEX IF EXISTS idx_knowledge_base_tenant;

ALTER TABLE knowledge_base
DROP COLUMN IF EXISTS tenant;