# Práctica 3.1 — Verificar el grafo de beans

## Enunciado

Spring Boot Actuator expone un endpoint que lista todos los beans registrados en el
contexto. Añade la dependencia al `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Y en `application.properties`:

```
management.endpoints.web.exposure.include=beans
```

Reinicia y accede a `http://localhost:8080/actuator/beans`. Busca en la respuesta JSON
los beans `libroController`, `bibliotecaService` y `repositorioLibros`.

1. ¿Qué campo indica el tipo concreto de cada bean?
2. ¿Qué campo muestra las dependencias que tiene cada bean?
3. ¿Cuál es el ámbito (`scope`) de los tres beans?

## Solución

Con la dependencia y la propiedad añadidas, `GET /actuator/beans` devuelve un JSON con
esta forma general (recortado a los tres beans que pide el enunciado; Actuator devuelve
además todos los beans internos del propio framework, que aquí se omiten):

```json
{
  "contexts": {
    "biblioteca-spring": {
      "beans": {
        "libroController": {
          "aliases": [],
          "scope": "singleton",
          "type": "com.javaesencial.biblioteca.controlador.LibroController",
          "resource": "file [.../LibroController.class]",
          "dependencies": ["bibliotecaService"]
        },
        "bibliotecaService": {
          "aliases": [],
          "scope": "singleton",
          "type": "com.javaesencial.biblioteca.servicio.BibliotecaService",
          "resource": "file [.../BibliotecaService.class]",
          "dependencies": ["repositorioLibros"]
        },
        "repositorioLibros": {
          "aliases": [],
          "scope": "singleton",
          "type": "com.javaesencial.biblioteca.repositorio.RepositorioLibros",
          "resource": "file [.../RepositorioLibros.class]",
          "dependencies": []
        }
      },
      "parentId": null
    }
  }
}
```

### 1. Campo del tipo concreto

`"type"`. Muestra el nombre completo de la clase (paquete incluido) que Spring
instanció para ese bean: `com.javaesencial.biblioteca.controlador.LibroController`,
`com.javaesencial.biblioteca.servicio.BibliotecaService` y
`com.javaesencial.biblioteca.repositorio.RepositorioLibros` respectivamente — coincide
con la clase real anotada con `@RestController`, `@Service` y `@Repository`.

### 2. Campo de las dependencias

`"dependencies"`. Es un array con el nombre (en `camelCase`, derivado del nombre de la
clase) de cada bean que este necesita para construirse. Refleja exactamente el grafo de
inyección por constructor del capítulo: `libroController` depende de
`["bibliotecaService"]` (el `BibliotecaService service` de su constructor);
`bibliotecaService` depende de `["repositorioLibros"]` (el `RepositorioLibros
repositorio` de su constructor); `repositorioLibros` no depende de ningún otro bean
(`[]`), porque `RepositorioLibros` no recibe nada por constructor — solo carga sus datos
iniciales en `@PostConstruct`.

### 3. Ámbito de los tres beans

`"scope": "singleton"` en los tres. Es el ámbito por defecto de cualquier bean de Spring
que no indique lo contrario con `@Scope`: el contenedor crea una única instancia de cada
uno y la reutiliza para todas las peticiones e inyecciones mientras la aplicación esté
viva — ninguno de los tres se ha marcado como `prototype` ni con ningún otro ámbito, así
que los tres siguen la regla por defecto.
