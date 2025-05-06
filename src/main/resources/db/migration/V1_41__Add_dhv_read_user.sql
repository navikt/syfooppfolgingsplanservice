DECLARE
view_exists NUMBER;
BEGIN
SELECT COUNT(*)
INTO view_exists
FROM all_views
WHERE view_name = 'GODKJENTPLAN_VIEW';

IF view_exists = 0 THEN
        EXECUTE IMMEDIATE 'CREATE VIEW GODKJENTPLAN_VIEW AS
                           SELECT SM_FNR, VIRKSOMHETSNUMMER, G.CREATED
                           FROM oppfoelgingsdialog o
                           INNER JOIN godkjentplan g
                           ON o.oppfoelgingsdialog_id = g.oppfoelgingsdialog_id
                           AND g.delt_med_nav = 1';
END IF;
END;
/

BEGIN
EXECUTE IMMEDIATE 'CREATE USER dvh_les IDENTIFIED BY some_password';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -01920 THEN
            DBMS_OUTPUT.PUT_LINE('User dvh_les already exists');
ELSE
            RAISE;
END IF;
END;
/

GRANT SELECT ON GODKJENTPLAN_VIEW TO dvh_les;