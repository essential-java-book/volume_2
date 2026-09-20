# Práctica 1.4 — Identificar clases reutilizables del Volumen 1

## Enunciado

Abre el proyecto `biblioteca` del Volumen 1 (Capítulo 21) y examina los paquetes
`dominio/` y `servicio/`. Responde:

1. ¿Qué clases de `dominio/` podrían copiarse a un proyecto Spring Boot sin cambios? ¿Por
   qué? (Pista: ninguna depende de la consola.)
2. ¿Qué clases del paquete `ui/` dependen de `Scanner` y `System.out` y por tanto no
   tienen sitio en una API REST?
3. `PersistenciaBiblioteca` leía y escribía los cinco CSV de `datos/`. ¿Qué parte de esa
   lógica (serializar, validar, escapar campos) sigue siendo útil? ¿Qué parte
   desaparecerá cuando lleguemos al Capítulo 9?
4. `Biblioteca` orquestaba la lógica de negocio sobre tres `Repositorio<T>`. ¿Crees que
   su código puede aprovecharse directamente? ¿Qué habría que añadirle para que Spring lo
   gestione como un bean?

## Solución

Esta práctica es de reflexión: no pide escribir código todavía (el proyecto Spring Boot
no arranca hasta el Capítulo 2), sino repasar el proyecto final del Volumen 1
(`com.javaesencial:biblioteca:1.0.0`, tag `v1.0.0`/`v1-cap21` del repositorio) con la
vista puesta en lo que se aprovecha y lo que no.

### 1. Clases de `dominio/` reutilizables sin cambios

El paquete `dominio/` del Volumen 1 contiene `Libro` (abstracta), `LibroFisico`,
`LibroDigital`, `Revista`, `Usuario`, `Prestamo`, `Solicitud`, las interfaces
`Identificable` y `Prestable`, y `BibliotecaException` con sus cinco subclases. Ninguna
de estas clases importa `java.util.Scanner` ni escribe con `System.out`: son POJOs de
dominio puro, con sus atributos, constructores, getters y la lógica de negocio que les
corresponde (por ejemplo, el cálculo de multas de `Prestamo`, o `puedeEliminarse()` en
`Usuario`). Eso es justo lo que las hace candidatas a copiarse sin cambios: una clase de
dominio bien diseñada no debería saber si quien la usa es un menú de consola o un
controlador REST — esa independencia es la que permite migrarla de un mundo al otro sin
tocarla.

Con matices, eso sí: el Volumen 2 no copia el dominio completo (`LibroFisico`,
`LibroDigital`, `Revista`, las multas por tramos y las listas de espera se quedan fuera
de su alcance — ver la tabla de correspondencias del §1.6), pero la clase `Libro` en
sí —la parte que sobrevive, con `id`, `titulo`, `autor`, `anio`— es exactamente ese tipo
de clase: pura, sin dependencias de consola, migrable.

### 2. Clases de `ui/` que no tienen sitio en una API REST

Todo el paquete `ui/` está construido alrededor de la interacción por consola:
`Main` (el punto de entrada con el bucle principal), `Consola` (las utilidades de
lectura con `Scanner` y de impresión con `System.out`) y los menús
(`MenuPrincipal`, `MenuLibros`, `MenuUsuarios`, `MenuPrestamos`, `MenuInformes`), que
encadenan llamadas a `Consola` para mostrar opciones y leer la elección del usuario.
Ninguna de estas clases tiene sentido en una API REST: una API no "muestra un menú" ni
"espera a que el usuario teclee una opción" — responde a peticiones HTTP con datos, en
JSON. Todo `ui/` desaparece en el Volumen 2, sustituido por `controlador/`
(`LibroController` y, más adelante, `PrestamoController`), que reciben peticiones HTTP en
vez de teclas.

### 3. `PersistenciaBiblioteca` y los CSV

`PersistenciaBiblioteca` cumplía dos funciones mezcladas: la lógica de *serialización* en
sí (convertir un objeto `Libro`/`Usuario`/`Prestamo` a una línea de texto con sus campos
separados por comas, y viceversa al leer) y el *mecanismo de almacenamiento* (abrir el
fichero, leerlo línea a línea, escribirlo de vuelta completo en cada guardado). De esas
dos partes, la que sigue siendo útil conceptualmente es la primera: la idea de mapear
cada atributo de un objeto a una representación persistente, y las validaciones que se
hacían al leer (comprobar que un campo no esté vacío, que un número se pueda parsear,
escapar comas dentro de un campo de texto) — esa misma preocupación existe en JPA, solo
que resuelta de otra forma: las anotaciones (`@Column`, `@NotNull`, etc.) declaran el
mapeo objeto-columna en vez de escribirlo a mano campo a campo.

Lo que desaparece por completo al llegar al Capítulo 9 es el mecanismo de
almacenamiento: leer y reescribir un fichero CSV entero en cada operación. Spring Data
JPA se encarga de la persistencia real contra una base de datos (H2 al principio), con
SQL generado automáticamente y una fila por operación en vez de un fichero completo — los
CSV de `datos/` (`catalogo.csv`, `usuarios.csv`, etc.) no tienen ningún papel en el
Volumen 2 más allá de servir de referencia conceptual de qué datos existían.

### 4. `Biblioteca` y los `Repositorio<T>`

El código de `Biblioteca` (las reglas de negocio: prestar un libro, registrar una
devolución, calcular una multa, comprobar si un usuario puede pedir más préstamos) sí se
puede aprovechar **como lógica**, no como clase tal cual. La propia `Biblioteca` recibía
sus tres `Repositorio<T>` (uno por libros, usuarios y préstamos) por constructor y
delegaba en ellos el acceso a los datos — esa idea de separar "reglas de negocio" de
"acceso a datos" es exactamente el mismo principio que separa `servicio/` de
`repositorio/` en el Volumen 2.

Para que Spring pueda gestionarla como un bean, a `Biblioteca` (que pasa a llamarse
`BibliotecaService` primero, y sus responsabilidades se reparten entre servicios más
pequeños — `LibroService` y, desde el Capítulo 10, `PrestamoService` — a medida que
avanza el volumen) le haría falta: la anotación `@Service` en la clase; sustituir la
inyección manual de `Repositorio<T>` por una interfaz `RepositorioLibros` anotada con
`@Repository` (y, desde el Capítulo 9, por una interfaz de Spring Data JPA que ni
siquiera necesita implementación); e inyectar esa dependencia por constructor, tal como
ya hacía con los `Repositorio<T>` del Volumen 1 — ese patrón de "recibir las
dependencias por constructor" no cambia, solo lo hace automáticamente el contenedor de
Spring en vez de tener que instanciarlas a mano en `Main`.
