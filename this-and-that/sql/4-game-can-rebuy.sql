ALTER TABLE game
    ADD canRebuy BOOLEAN DEFAULT TRUE;
UPDATE game
SET canRebuy = false;
