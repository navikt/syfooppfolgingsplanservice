-- ROLLBACK-START
------------------

-- DROP SEQUENCE VEILEDER_BEHANDLING_ID_SEQ;
-- DROP TABLE VEILEDER_BEHANDLING;

---------------
-- ROLLBACK-END

CREATE SEQUENCE VEILEDER_BEHANDLING_ID_SEQ
START WITH 1
INCREMENT BY 1;

CREATE TABLE VEILEDER_BEHANDLING (
  oppgave_id                  NUMBER(19, 0) NOT NULL,
  oppgave_uuid                VARCHAR2(50) UNIQUE NOT NULL,
  godkjentplan_id             NUMBER(19, 0) NOT NULL,
  tildelt_ident               VARCHAR2(20),
  tildelt_enhet               VARCHAR2(20),
  opprettet_dato              TIMESTAMP NOT NULL,
  sist_endret                 TIMESTAMP NOT NULL,
  sist_endret_av              VARCHAR2(20),
  status                      VARCHAR2(100) DEFAULT 'IKKE_LEST' NOT NULL,
  CONSTRAINT VEILEDER_BEHANDLING_PK PRIMARY KEY (oppgave_id),
  CONSTRAINT GODKJ_VEILEDER_BEHANDLING_FK FOREIGN KEY (godkjentplan_id) REFERENCES GODKJENTPLAN (godkjentplan_id)
);

CREATE OR REPLACE FUNCTION generateUUID
  RETURN
    VARCHAR2
  IS
    guid VARCHAR2(32);
    uuid VARCHAR2(36);
  BEGIN
    guid := SYS_GUID();
    uuid := lower(
      substr(guid, 1,8) || '-' ||
      substr(guid, 9,4) || '-' ||
      substr(guid, 13,4) || '-' ||
      substr(guid, 17,4) || '-' ||
      substr(guid, 21)
    );
  RETURN uuid;
END;
/

BEGIN
  INSERT INTO VEILEDER_BEHANDLING (oppgave_id, oppgave_uuid, godkjentplan_id, tildelt_enhet, opprettet_dato, sist_endret)
    SELECT
      VEILEDER_BEHANDLING_ID_SEQ.NEXTVAL,
      generateUUID(),
      GODKJENTPLAN_ID,
      TILDELT_ENHET,
      DELT_MED_NAV_TIDSPUNKT,
      DELT_MED_NAV_TIDSPUNKT
    FROM GODKJENTPLAN
    WHERE TILDELT_ENHET IS NOT NULL;
END;
/

DROP FUNCTION generateUUID