ALTER TABLE GODKJENTPLAN
  ADD (
  delt_med_fastlege_tidspunkt TIMESTAMP,
  delt_med_fastlege CHAR DEFAULT 0 CHECK (delt_med_fastlege IN (0, 1)) NOT NULL
  );