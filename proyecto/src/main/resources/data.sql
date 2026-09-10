INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('El Quijote', 'Miguel de Cervantes', 1605, '978-84-376-0494-7');
INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('1984', 'George Orwell', 1949, '978-84-663-2674-7');
INSERT INTO libro (titulo, autor, anio, isbn) VALUES ('Dune', 'Frank Herbert', 1965, '978-84-450-7715-6');

INSERT INTO usuario (nombre, email) VALUES ('Ana García', 'ana@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Pedro López', 'pedro@bib.es');
INSERT INTO usuario (nombre, email) VALUES ('Laura Martín', 'laura@bib.es');

INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (1, 1, '2026-08-01', NULL);
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (2, 2, '2026-08-05', '2026-08-20');
INSERT INTO prestamo (usuario_id, libro_id, fecha_prestamo, fecha_devolucion) VALUES (3, 3, '2026-08-10', NULL);

-- Contraseñas en claro: admin123 / user123 (BCrypt, cost 10).
INSERT INTO credencial (username, password, rol) VALUES ('admin', '$2b$10$EL9K75/f1qttirrbOc0g7utVVIK/BLmkytoNkIt/YcRAr/BaT7s5C', 'ADMIN');
INSERT INTO credencial (username, password, rol) VALUES ('bibliotecario', '$2b$10$A1o3vLZV/nAvkKGU9S80AuTQHLdOPX2hE8OlqASjk0aQMsnNgkV2.', 'USER');
