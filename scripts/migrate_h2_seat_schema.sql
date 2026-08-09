ALTER TABLE seat ADD COLUMN IF NOT EXISTS seat_sequence INTEGER;
ALTER TABLE seat ADD COLUMN IF NOT EXISTS seat_label VARCHAR(50);

UPDATE seat
SET seat_sequence = CAST(seat_number AS INTEGER),
    seat_label = section || '-' || seat_number
WHERE seat_sequence IS NULL AND REGEXP_LIKE(seat_number, '^[0-9]+$');

UPDATE seat s
SET seat_sequence =
      (SELECT COALESCE(MAX(x.seat_sequence), 0)
       FROM seat x
       WHERE x.concert_id = s.concert_id AND x.section = s.section)
      +
      (SELECT COUNT(*)
       FROM seat x
       WHERE x.concert_id = s.concert_id
         AND x.section = s.section
         AND x.seat_sequence IS NULL
         AND x.id <= s.id),
    seat_label = section || '-' || seat_number
WHERE seat_sequence IS NULL;

ALTER TABLE seat ALTER COLUMN seat_sequence SET NOT NULL;
ALTER TABLE seat ALTER COLUMN seat_label SET NOT NULL;

ALTER TABLE reservation ADD COLUMN IF NOT EXISTS reserved_price NUMERIC(19, 0);

UPDATE reservation r
SET reserved_price = (SELECT s.price FROM seat s WHERE s.id = r.seat_id)
WHERE reserved_price IS NULL;

ALTER TABLE seat DROP CONSTRAINT IF EXISTS UKDE7XO73Q0XNOID50YSE80WL1G;
ALTER TABLE seat DROP COLUMN IF EXISTS seat_number;
