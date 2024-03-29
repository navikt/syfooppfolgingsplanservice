-- ROLLBACK-START
------------------
-- DROP SEQUENCE PLANLPS_RETRY_ID_SEQ;
-- DROP TABLE OPPFOLGINGSPLANLPS_RETRY;
---------------
-- ROLLBACK-END

CREATE SEQUENCE PLANLPS_RETRY_ID_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE OPPFOLGINGSPLANLPS_RETRY (
  oppfolgingsplanlps_retry_id       NUMBER(19, 0) NOT NULL,
  archive_reference                 VARCHAR(40) NOT NULL,
  xml                               CLOB NOT NULL,
  opprettet                         TIMESTAMP NOT NULL,
  CONSTRAINT OPPFOLGINGSPLANLPS_RETRY_PK PRIMARY KEY (oppfolgingsplanlps_retry_id)
);
