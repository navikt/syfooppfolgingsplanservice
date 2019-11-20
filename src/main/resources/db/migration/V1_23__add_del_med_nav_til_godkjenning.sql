-- ROLLBACK-START
------------------
-- ALTER TABLE GODJENNING DROP ( DEL_MED_NAV );
---------------
-- ROLLBACK-END

ALTER TABLE GODKJENNING
  ADD (
    del_med_nav CHAR DEFAULT 0 CHECK (del_med_nav IN (0, 1))  NOT NULL
  );
