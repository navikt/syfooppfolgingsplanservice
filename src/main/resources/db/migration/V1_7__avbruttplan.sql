-- ROLLBACK-START
------------------
-- ALTER TABLE GODKJENTPLAN DROP ( AVBRUTT_TIDSPUNKT, AVBRUTT_AV );
---------------
-- ROLLBACK-END

ALTER TABLE GODKJENTPLAN
  ADD (
  AVBRUTT_TIDSPUNKT TIMESTAMP,
  AVBRUTT_AV VARCHAR(13)
);

