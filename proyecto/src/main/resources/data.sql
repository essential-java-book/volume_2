INSERT INTO libro (titulo, autor, anio) VALUES ('El Quijote', 'Miguel de Cervantes', 1605);
INSERT INTO libro (titulo, autor, anio) VALUES ('1984', 'George Orwell', 1949);
INSERT INTO libro (titulo, autor, anio) VALUES ('Dune', 'Frank Herbert', 1965);

INSERT INTO usuario (nombre, email) VALUES ('Ana García', 'ana@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Pedro López', 'pedro@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Laura Martín', 'laura@bib.es');

INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (1, 1, '2026-08-01', NULL);
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (2, 2, '2026-08-05', '2026-08-20');
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (3, 3, '2026-08-10', NULL);
