# Práctica 4.2 — Perfiles `dev` y `prod` en acción

## Enunciado

1. Crea `application-dev.properties` con `logging.level.com.javaesencial=DEBUG` y
   `biblioteca.max-libros=10`.
2. Crea `application-prod.properties` con `logging.level.com.javaesencial=WARN` y
   `biblioteca.max-libros=500`.
3. Activa el perfil `dev` en `application.properties` con
   `spring.profiles.active=dev`.
4. Arrancan con `mvn spring-boot:run`. ¿Qué nivel de log ves en la consola? ¿Cuánto es
   `maxLibros` según `GET /info`?
5. Cambia a `spring.profiles.active=prod` y repite. ¿Cambia el nivel de log? ¿Cambia
   `maxLibros`?

## Solución

> **Nota:** esta práctica pide valores de `max-libros` distintos a propósito (10 en
> `dev`, 500 en `prod`) para que el efecto del cambio de perfil se note con claridad al
> comparar el JSON de `/info`. Es un valor de ejercicio — el proyecto real usa 500 en
> ambos perfiles una vez cerrado el capítulo (ver §4.8), pero para esta práctica interesa
> justamente que sean distintos.

`application.properties` (fichero base, sin perfil todavía activado en este paso):

```properties
spring.application.name=biblioteca-spring
```

`application-dev.properties`:

```properties
logging.level.com.javaesencial=DEBUG
biblioteca.nombre=Biblioteca Municipal "El Quijote"
biblioteca.version=2.0
biblioteca.max-libros=10
biblioteca.modo-mantenimiento=false
```

`application-prod.properties`:

```properties
logging.level.com.javaesencial=WARN
biblioteca.nombre=Biblioteca Municipal "El Quijote"
biblioteca.version=2.0
biblioteca.max-libros=500
biblioteca.modo-mantenimiento=false
```

(`biblioteca.nombre`, `biblioteca.version` y `biblioteca.modo-mantenimiento` son
obligatorios porque `BibliotecaConfig` los declara sin valor por defecto; si faltaran, la
aplicación fallaría al arrancar con el mismo error de *placeholder* no resuelto que ya se
vio en la sección "Errores Comunes" del capítulo.)

### 3. Activar `dev`

```properties
spring.application.name=biblioteca-spring
spring.profiles.active=dev
```

### 4. Con `dev` activo

En la consola, junto al *banner* de arranque:

```terminal
The following 1 profile is active: "dev"
```

Y, a partir de ahí, con el nivel `DEBUG` activo para `com.javaesencial` (y todos sus
subpaquetes, incluido `com.javaesencial.biblioteca`), cualquier log de nivel `DEBUG` o
superior de las clases del proyecto se muestra en consola —de momento ninguna clase
registra logs todavía; eso llega en la Práctica 4.4—.

```terminal
GET http://localhost:8080/info
```

```json
{
  "nombre": "Biblioteca Municipal \"El Quijote\"",
  "version": "2.0",
  "maxLibros": 10,
  "modoMantenimiento": false
}
```

`maxLibros` vale **10**, el valor de `application-dev.properties`.

### 5. Cambiar a `prod`

```properties
spring.application.name=biblioteca-spring
spring.profiles.active=prod
```

**Sí cambia el nivel de log**: la consola pasa a mostrar

```terminal
The following 1 profile is active: "prod"
```

y, con `logging.level.com.javaesencial=WARN`, solo se mostrarían logs de nivel `WARN` o
`ERROR` de las clases del proyecto — cualquier `DEBUG`/`INFO` que se hubiera añadido
quedaría silenciado.

**Y sí cambia `maxLibros`:**

```json
{
  "nombre": "Biblioteca Municipal \"El Quijote\"",
  "version": "2.0",
  "maxLibros": 500,
  "modoMantenimiento": false
}
```

Ahora vale **500**, el de `application-prod.properties`. Spring Boot solo carga **uno**
de los dos ficheros por perfil (el que coincide con `spring.profiles.active`, además del
`application.properties` base, que se combina siempre): cambiar el valor de
`spring.profiles.active` es lo único que hace falta para que toda la aplicación arranque
con un conjunto de valores distinto, sin tocar ni recompilar ningún código.
