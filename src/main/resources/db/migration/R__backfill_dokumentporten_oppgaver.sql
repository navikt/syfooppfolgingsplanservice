INSERT INTO ASYNK_OPPGAVE (
  asynk_oppgave_id,
  opprettet_tidspunkt,
  oppgavetype,
  avhengig_av,
  antall_forsoek,
  ressurs_id
)
SELECT
  ASYNK_OPPGAVE_ID_SEQ.NEXTVAL,
  LOCALTIMESTAMP,
  'OPPFOELGINGSDIALOG_DOKUMENTPORTEN_SEND',
  NULL,
  0,
  TO_CHAR(g.oppfoelgingsdialog_id)
FROM GODKJENTPLAN g
WHERE g.created > TIMESTAMP '2026-06-15 11:45:00.000'
  AND NOT EXISTS (
    SELECT 1
    FROM ASYNK_OPPGAVE a
    WHERE a.oppgavetype = 'OPPFOELGINGSDIALOG_DOKUMENTPORTEN_SEND'
      AND a.ressurs_id = TO_CHAR(g.oppfoelgingsdialog_id)
  );
