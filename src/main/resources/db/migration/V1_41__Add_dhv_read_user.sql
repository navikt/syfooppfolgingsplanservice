DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.views WHERE table_name = 'godkjentplan_view') THEN
CREATE VIEW GODKJENTPLAN_VIEW AS
SELECT SM_FNR, VIRKSOMHETSNUMMER, G.CREATED
FROM oppfoelgingsdialog o
         INNER JOIN godkjentplan g
                    ON o.oppfoelgingsdialog_id = g.oppfoelgingsdialog_id
                        AND g.delt_med_nav = 1;
END IF;
END
$$;

DO $$
BEGIN
        CREATE USER "dvh_les";
EXCEPTION WHEN DUPLICATE_OBJECT THEN
        RAISE NOTICE 'not creating role dvh_les -- it already exists';
END
$$;

GRANT SELECT ON GODKJENTPLAN_VIEW TO "dvh_les";
