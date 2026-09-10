INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('El Quijote', 'Miguel de Cervantes', 1605, '978-84-376-0494-7');
INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('1984', 'George Orwell', 1949, '978-84-663-2674-7');
INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('Dune', 'Frank Herbert', 1965, '978-84-450-7715-6');

INSERT INTO usuario (nombre, email) VALUES ('Ana García', 'ana@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Pedro López', 'pedro@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Laura Martín', 'laura@bib.es');

INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (1, 1, '2026-08-01', NULL);
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (2, 2, '2026-08-05', '2026-08-20');
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (3, 3, '2026-08-10', NULL);
