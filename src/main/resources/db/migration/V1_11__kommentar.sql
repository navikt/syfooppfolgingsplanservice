-- ROLLBACK-START
------------------
-- DROP SEQUENCE KOMMENTAR_ID_SEQ;
-- DROP TABLE KOMMENTAR;
-- ALTER TABLE TILTAK DROP ( status, gjennomfoering );

---------------
-- ROLLBACK-END

ALTER TABLE TILTAK MODIFY BESKRIVELSE VARCHAR(2400) DEFAULT NULL;

ALTER TABLE TILTAK
  ADD (
  status                        VARCHAR(13) DEFAULT 'FORSLAG' NOT NULL,
  gjennomfoering                VARCHAR(2400)
);

CREATE SEQUENCE KOMMENTAR_ID_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE KOMMENTAR (
  kommentar_id                  NUMBER(19, 0) NOT NULL,
  tiltak_id                     NUMBER(19, 0) NOT NULL,
  tekst                         VARCHAR(1200) NOT NULL,
  sist_endret_av                VARCHAR(13)   NOT NULL,
  sist_endret_dato              TIMESTAMP     NOT NULL,
  opprettet_av                  VARCHAR(13)   NOT NULL,
  opprettet_dato                TIMESTAMP     NOT NULL,
  CONSTRAINT KOMMENTAR_PK PRIMARY KEY (kommentar_id),
  CONSTRAINT KOMMENTAR_TILTAK_FK FOREIGN KEY (tiltak_id) REFERENCES TILTAK (tiltak_id)
);


CREATE INDEX kommentar_tiltakid_index
  ON KOMMENTAR (TILTAK_ID);
