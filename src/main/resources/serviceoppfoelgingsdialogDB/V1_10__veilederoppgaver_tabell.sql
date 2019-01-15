CREATE TABLE VEILEDEROPPGAVE (
  veilederoppgave_id    NUMBER(19, 0) NOT NULL,
  oppfoelgingsdialog_id NUMBER(19, 0) NOT NULL,
  CONSTRAINT VEILEDEROPPGAVE_PK PRIMARY KEY (veilederoppgave_id, oppfoelgingsdialog_id),
  CONSTRAINT VEILEDEROPPGAVE_PLAN_FK FOREIGN KEY (oppfoelgingsdialog_id) REFERENCES OPPFOELGINGSDIALOG (oppfoelgingsdialog_id)
);


CREATE INDEX vlroppg_oppdialogid_index
  ON VEILEDEROPPGAVE (OPPFOELGINGSDIALOG_ID);
