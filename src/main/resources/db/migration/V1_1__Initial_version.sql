-- ROLLBACK-START
------------------
-- DROP SEQUENCE OPPFOELGINGSDIALOG_ID_SEQ;
-- DROP SEQUENCE GODKJENTPLAN_ID_SEQ;
-- DROP SEQUENCE GYLDIGHETSTIDSPUNKT_ID_SEQ;
-- DROP SEQUENCE ARBEIDSOPPGAVE_ID_SEQ;
-- DROP SEQUENCE TILTAK_ID_SEQ;
-- DROP SEQUENCE GJENNOMFOERING_ID_SEQ;
-- DROP SEQUENCE GODKJENNING_ID_SEQ;
-- DROP TABLE GODKJENTPLAN;
-- DROP TABLE DOKUMENT;
-- DROP TABLE TILTAK;
-- DROP TABLE ARBEIDSOPPGAVE;
-- DROP TABLE GODKJENNING;
-- DROP TABLE OPPFOELGINGSDIALOG;

---------------
-- ROLLBACK-END

CREATE SEQUENCE OPPFOELGINGSDIALOG_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE GODKJENTPLAN_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE ARBEIDSOPPGAVE_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE TILTAK_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE GJENNOMFOERING_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE GODKJENNING_ID_SEQ START WITH 1 INCREMENT BY 1;


CREATE TABLE OPPFOELGINGSDIALOG (
  oppfoelgingsdialog_id       NUMBER(19, 0) NOT NULL,
  aktoer_id                   VARCHAR(13)   NOT NULL,
  virksomhetsnummer           VARCHAR(9)    NOT NULL,
  opprettet_av                VARCHAR(13)   NOT NULL,
  created                     TIMESTAMP     NOT NULL,
  arbeidsgiver_sist_innlogget TIMESTAMP,
  sykmeldt_sist_innlogget     TIMESTAMP,
  sist_endret_av              VARCHAR(13)   NOT NULL,
  sist_endret                 TIMESTAMP     NOT NULL,
  CONSTRAINT OPPFOELGINGSDIALOG_PK PRIMARY KEY (oppfoelgingsdialog_id)
);


CREATE TABLE GODKJENNING (
  godkjenning_id        NUMBER(19, 0)                      NOT NULL,
  oppfoelgingsdialog_id NUMBER(19, 0)                      NOT NULL,
  aktoer_id             VARCHAR(13)                        NOT NULL,
  godkjent              CHAR CHECK (godkjent IN (0, 1))    NOT NULL,
  beskrivelse           VARCHAR(1200),
  fom                   TIMESTAMP,
  tom                   TIMESTAMP,
  evalueres             TIMESTAMP,
  created               TIMESTAMP                          NOT NULL,
  CONSTRAINT GODKJENNING_PK PRIMARY KEY (godkjenning_id),
  CONSTRAINT GODKJENNING_PLAN_FK FOREIGN KEY (oppfoelgingsdialog_id) REFERENCES OPPFOELGINGSDIALOG (oppfoelgingsdialog_id)
);

CREATE TABLE GODKJENTPLAN (
  godkjentplan_id        NUMBER(19, 0)                                   NOT NULL,
  oppfoelgingsdialog_id  NUMBER(19, 0)                                   NOT NULL,
  dokument_uuid          VARCHAR(100) UNIQUE                             NOT NULL,
  versjon                NUMBER(3)                                       NOT NULL,
  samtykke_sykmeldt      CHAR CHECK (samtykke_sykmeldt IN (0, 1))        NOT NULL,
  samtykke_arbeidsgiver  CHAR CHECK (samtykke_arbeidsgiver IN (0, 1))    NOT NULL,
  created                TIMESTAMP                                       NOT NULL,
  fom                    TIMESTAMP                                       NOT NULL,
  tom                    TIMESTAMP                                       NOT NULL,
  evalueres              TIMESTAMP                                       NOT NULL,
  delt_med_nav_tidspunkt TIMESTAMP,
  tvungen_godkjenning    CHAR CHECK (tvungen_godkjenning IN (0, 1))      NOT NULL,
  delt_med_nav           CHAR CHECK (delt_med_nav IN (0, 1))             NOT NULL,
  CONSTRAINT GODKJENTPLAN_PK PRIMARY KEY (godkjentplan_id),
  CONSTRAINT OPPFO_GODKJENTPLAN_FK FOREIGN KEY (oppfoelgingsdialog_id) REFERENCES OPPFOELGINGSDIALOG (oppfoelgingsdialog_id)
);

CREATE TABLE ARBEIDSOPPGAVE (
  arbeidsoppgave_id            NUMBER(19, 0) NOT NULL,
  oppfoelgingsdialog_id        NUMBER(19, 0) NOT NULL,
  navn                         VARCHAR(120)  NOT NULL,
  er_vurdert_av_sykmeldt       CHAR CHECK (er_vurdert_av_sykmeldt IN (0, 1)),
  opprettet_av                 VARCHAR(13)   NOT NULL,
  sist_endret_av               VARCHAR(13)   NOT NULL,
  sist_endret_dato             TIMESTAMP     NOT NULL,
  opprettet_dato               TIMESTAMP     NOT NULL,
  kan_gjennomf_tilrettelegging CHAR CHECK (kan_gjennomf_tilrettelegging IN (0, 1)),
  kan_ikke_gjennomfoeres       CHAR CHECK (kan_ikke_gjennomfoeres IN (0, 1)),
  kan_gjennomfoeres_normalt    CHAR CHECK (kan_gjennomfoeres_normalt IN (0, 1)),
  paa_annet_sted               CHAR CHECK (paa_annet_sted IN (0, 1)),
  med_mer_tid                  CHAR CHECK (med_mer_tid IN (0, 1)),
  med_hjelp                    CHAR CHECK (med_hjelp IN (0, 1)),
  beskrivelse                  VARCHAR(1200),
  kan_ikke_beskrivelse         VARCHAR(1200),
  CONSTRAINT ARBEIDSOPPGAVE_PK PRIMARY KEY (arbeidsoppgave_id),
  CONSTRAINT OPPFO_ARBOPG_FK FOREIGN KEY (oppfoelgingsdialog_id) REFERENCES OPPFOELGINGSDIALOG (oppfoelgingsdialog_id)
);

CREATE TABLE TILTAK (
  tiltak_id                     NUMBER(19, 0) NOT NULL,
  oppfoelgingsdialog_id         NUMBER(19, 0) NOT NULL,
  navn                          VARCHAR(120)  NOT NULL,
  knyttet_til_arbeidsoppgave_id NUMBER(19, 0),
  fom                           DATE,
  tom                           DATE,
  beskrivelse                   VARCHAR(1200),
  opprettet_av                  VARCHAR(13)   NOT NULL,
  sist_endret_av                VARCHAR(13)   NOT NULL,
  sist_endret_dato              TIMESTAMP     NOT NULL,
  opprettet_dato                TIMESTAMP     NOT NULL,
  CONSTRAINT TILTAK_PK PRIMARY KEY (tiltak_id),
  CONSTRAINT OPPFO_TILTAK_FK FOREIGN KEY (oppfoelgingsdialog_id) REFERENCES OPPFOELGINGSDIALOG (oppfoelgingsdialog_id)
);

CREATE TABLE DOKUMENT (
  dokument_uuid VARCHAR(100) UNIQUE NOT NULL,
  pdf           BLOB                NOT NULL,
  xml           LONG VARCHAR        NOT NULL
);