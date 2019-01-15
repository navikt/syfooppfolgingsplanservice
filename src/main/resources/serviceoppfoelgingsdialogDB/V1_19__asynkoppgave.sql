-- ROLLBACK-START
------------------
-- DROP SEQUENCE ASYNK_OPPGAVE_ID_SEQ;
-- DROP TABLE ASYNK_OPPGAVE;
---------------
-- ROLLBACK-END

CREATE SEQUENCE ASYNK_OPPGAVE_ID_SEQ
  START WITH 1
  INCREMENT BY 1;

CREATE TABLE ASYNK_OPPGAVE (
  asynk_oppgave_id    NUMBER(19, 0) NOT NULL,
  opprettet_tidspunkt TIMESTAMP     NOT NULL,
  oppgavetype         VARCHAR(255)  NOT NULL,
  avhengig_av         NUMBER(19, 0),
  antall_forsoek      NUMBER(19, 0) NOT NULL,
  ressurs_id          VARCHAR(255)  NOT NULL,
  CONSTRAINT ASYNK_OPPGAVE_PK PRIMARY KEY (asynk_oppgave_id)
);