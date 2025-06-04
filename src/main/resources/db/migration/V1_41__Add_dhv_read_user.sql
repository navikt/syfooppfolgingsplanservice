CREATE OR REPLACE FORCE EDITIONABLE VIEW GODKJENTPLAN_VIEW AS
                           SELECT SM_FNR, VIRKSOMHETSNUMMER, O.CREATED AS oppfoelgingsdialog_created,
                                  G.CREATED AS GODKJENTPLAN_CREATED, G.DELT_MED_NAV_TIDSPUNKT
                           FROM oppfoelgingsdialog o
                           INNER JOIN godkjentplan g
                           ON o.oppfoelgingsdialog_id = g.oppfoelgingsdialog_id
                           AND g.delt_med_nav = 1;
