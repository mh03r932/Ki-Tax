delete from sequence where sequence_type='FALL_NUMMER' and mandant_id ='e3736eb8-6eef-40ef-9e52-96ab48d8f220';

INSERT INTO sequence (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
  (SELECT DISTINCT
     '06c1e5d5-48c0-4f2d-af25-251b15de8ceb',
     '2016-09-27 00:00:00',
     '2016-09-27 00:00:00',
     'flyway',
     'flyway',
     0,
     'FALL_NUMMER',
      GREATEST(coalesce(MAX(fall.fall_nummer), 100), 100),
     -- aktuell hoechste fallnummer oder 100  wenn keine vorhanden  (coalesce gibt ersten nonnull wert zurueck)
     'e3736eb8-6eef-40ef-9e52-96ab48d8f220' # mandant id des fixen mandanten
   FROM fall);
