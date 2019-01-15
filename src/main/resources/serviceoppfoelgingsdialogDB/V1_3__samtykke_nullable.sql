-- ROLLBACK-START
------------------
-- ALTER TABLE GODKJENTPLAN MODIFY samtykke_sykmeldt CHAR CHECK (samtykke_sykmeldt IN (0, 1)) NOT NULL;
-- ALTER TABLE GODKJENTPLAN MODIFY samtykke_arbeidsgiver CHAR CHECK (samtykke_arbeidsgiver IN (0, 1)) NOT NULL;

---------------
-- ROLLBACK-END

ALTER TABLE GODKJENTPLAN
  MODIFY (
  samtykke_sykmeldt CHAR CHECK (samtykke_sykmeldt IN (0, 1)) NULL,
  samtykke_arbeidsgiver CHAR CHECK (samtykke_arbeidsgiver IN (0, 1)) NULL
  );
