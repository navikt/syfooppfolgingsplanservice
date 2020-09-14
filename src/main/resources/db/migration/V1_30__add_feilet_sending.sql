-- ROLLBACK-START
------------------
-- DROP SEQUENCE FEILET_SENDING_ID_SEQ;
-- DROP TABLE FEILET_SENDING;
---------------
-- ROLLBACK-END

CREATE SEQUENCE FEILET_SENDING_ID_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE FEILET_SENDING (
  id                          NUMBER(19, 0) NOT NULL,
  oppfolgingsplanlps_id       NUMBER(19, 0) NOT NULL,
  number_of_tries             NUMBER(2) NOT NULL,
  max_retries                 NUMBER(2) NOT NULL,
  opprettet                   TIMESTAMP NOT NULL,
  sist_endret                 TIMESTAMP NOT NULL,
  CONSTRAINT FEILET_SENDING_PK PRIMARY KEY (id),
  CONSTRAINT FEILET_SENDING_OPPFOLGINGSPLAN_FK FOREIGN KEY (oppfolgingsplanlps_id) REFERENCES OPPFOLGINGSPLANLPS (oppfolgingsplanlps_id)
);
