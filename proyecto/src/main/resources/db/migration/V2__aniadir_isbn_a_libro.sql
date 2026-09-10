-- V2: el ISBN vuelve al catálogo (existía en el Volumen 1, se había
-- dejado fuera de la versión reducida de los Capítulos 3-11).

ALTER TABLE libro ADD COLUMN isbn VARCHAR(20);
