-- ROLLBACK-START
------------------
-- DROP SEQUENCE OPPFOLGINGSPLANLPS_ID_SEQ;
-- DROP TABLE OPPFOLGINGSPLANLPS;
---------------
-- ROLLBACK-END

CREATE SEQUENCE OPPFOLGINGSPLANLPS_ID_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE OPPFOLGINGSPLANLPS (
  oppfolgingsplanlps_id       NUMBER(19, 0) NOT NULL,
  oppfolgingsplanlps_uuid     VARCHAR(36) UNIQUE NOT NULL,
  fnr                         VARCHAR(11) NOT NULL,
  virksomhetsnummer           VARCHAR(9)  NOT NULL,
  opprettet                   TIMESTAMP NOT NULL,
  sist_endret                 TIMESTAMP NOT NULL,
  pdf                         CLOB,
  xml                         LONG VARCHAR NOT NULL,
  delt_med_nav                CHAR CHECK (delt_med_nav IN (0, 1)) NOT NULL,
  del_med_fastlege            CHAR CHECK (del_med_fastlege IN (0, 1)) NOT NULL,
  delt_med_fastlege           CHAR DEFAULT 0 CHECK (delt_med_fastlege IN (0, 1)) NOT NULL,
  CONSTRAINT OPPFOLGINGSPLANLPS_PK PRIMARY KEY (oppfolgingsplanlps_id)
);
