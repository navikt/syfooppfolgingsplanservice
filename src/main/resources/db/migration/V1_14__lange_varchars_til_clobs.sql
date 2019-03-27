alter table DOKUMENT
  MODIFY xml CLOB;

select INDEX_NAME
from user_indexes
where  table_name='DOKUMENT' and index_type='NORMAL';

BEGIN
  FOR index_entry IN (select INDEX_NAME
                      from user_indexes
                      where  table_name='DOKUMENT' and
                             index_type='NORMAL')
  LOOP
    dbms_output.put_line('ALTER INDEX ' || index_entry.INDEX_NAME || ' REBUILD');
    EXECUTE IMMEDIATE 'ALTER INDEX ' || index_entry.INDEX_NAME || ' REBUILD';
  END LOOP;
END;
commit;

ALTER TABLE ARBEIDSOPPGAVE
  ADD (tmpclob  CLOB);

UPDATE ARBEIDSOPPGAVE SET tmpclob=beskrivelse;
COMMIT;

ALTER TABLE ARBEIDSOPPGAVE DROP COLUMN beskrivelse;

ALTER TABLE ARBEIDSOPPGAVE
  RENAME COLUMN tmpclob TO beskrivelse;


ALTER TABLE ARBEIDSOPPGAVE
  ADD (tmpclob  CLOB);

UPDATE ARBEIDSOPPGAVE SET tmpclob=kan_ikke_beskrivelse;
COMMIT;

ALTER TABLE ARBEIDSOPPGAVE DROP COLUMN kan_ikke_beskrivelse;

ALTER TABLE ARBEIDSOPPGAVE
  RENAME COLUMN tmpclob TO kan_ikke_beskrivelse;


ALTER TABLE GODKJENNING
  ADD (tmpclob  CLOB);

UPDATE GODKJENNING SET tmpclob=beskrivelse;
COMMIT;

ALTER TABLE GODKJENNING DROP COLUMN beskrivelse;

ALTER TABLE GODKJENNING
  RENAME COLUMN tmpclob TO beskrivelse;



ALTER TABLE KOMMENTAR
  ADD (tmpclob  CLOB);

UPDATE KOMMENTAR SET tmpclob=tekst;
COMMIT;

ALTER TABLE KOMMENTAR DROP COLUMN tekst;

ALTER TABLE KOMMENTAR
  RENAME COLUMN tmpclob TO tekst;



ALTER TABLE TILTAK
  ADD (tmpclob  CLOB);

UPDATE TILTAK SET tmpclob=beskrivelse;
COMMIT;

ALTER TABLE TILTAK DROP COLUMN beskrivelse;

ALTER TABLE TILTAK
  RENAME COLUMN tmpclob TO beskrivelse;

ALTER TABLE TILTAK
  ADD (tmpclob  CLOB);

UPDATE TILTAK SET tmpclob=gjennomfoering;
COMMIT;

ALTER TABLE TILTAK DROP COLUMN gjennomfoering;

ALTER TABLE TILTAK
  RENAME COLUMN tmpclob TO gjennomfoering;
