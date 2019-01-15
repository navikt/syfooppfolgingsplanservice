-- ROLLBACK-START
------------------
-- DROP SEQUENCE SAMTYKKE_ID_SEQ;
-- DROP TABLE SAMTYKKE;
---------------
-- ROLLBACK-END

CREATE SEQUENCE SAMTYKKE_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE SAMTYKKE (
  samtykke_id           NUMBER(19, 0)                          NOT NULL,
  oppfoelgingsdialog_id NUMBER(19, 0)                          NOT NULL,
  versjon               NUMBER(3)                              NOT NULL,
  aktoer_id             VARCHAR(13)                            NOT NULL,
  samtykke              CHAR CHECK (samtykke IN (0, 1))        NOT NULL,
  CONSTRAINT SAMTYKKE_PK PRIMARY KEY (samtykke_id),
  CONSTRAINT SAMTYKKE_PLAN_FK FOREIGN KEY (oppfoelgingsdialog_id) REFERENCES OPPFOELGINGSDIALOG (oppfoelgingsdialog_id)
);


