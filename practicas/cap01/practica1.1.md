# Práctica 1.1 — Explorar Spring Initializr

## Enunciado

Entra en [https://start.spring.io](https://start.spring.io) y genera un proyecto con la
siguiente configuración: tipo *Maven Project*, lenguaje *Java*, versión de Spring Boot la
más reciente estable, grupo `com.javaesencial`, artefacto `biblioteca-spring`, empaquetado
*JAR*, Java 21. Añade la dependencia *Spring Web*.

Una vez descargado y descomprimido el ZIP, responde:

1. ¿Qué archivos hay en la raíz del proyecto? ¿Cuál es el punto de entrada de la aplicación?
2. Abre el `pom.xml`. ¿Qué versión de Spring Boot aparece en el bloque `<parent>`? ¿Qué
   dependencias se han añadido?
3. ¿Hay algún archivo en `src/main/resources/`? ¿Tiene contenido?
4. Abre la clase principal. ¿Qué anotación lleva? ¿Qué hace la llamada
   `SpringApplication.run(...)`?

## Solución

### 1. Archivos en la raíz y punto de entrada

Spring Initializr genera siempre la misma estructura, independientemente de las
dependencias elegidas:

```
biblioteca-spring/
├── .gitignore
├── .mvn/
│   └── wrapper/
│       ├── maven-wrapper.properties
│       └── maven-wrapper.jar
├── mvnw
├── mvnw.cmd
├── pom.xml
├── HELP.md
└── src/
    ├── main/
    │   ├── java/com/javaesencial/biblioteca/
    │   │   └── BibliotecaSpringApplication.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/
    │       └── templates/
    └── test/
        └── java/com/javaesencial/biblioteca/
            └── BibliotecaSpringApplicationTests.java
```

El punto de entrada de la aplicación es la clase que Initializr genera con el nombre
`<Artefacto>Application` (en `PascalCase`, quitando los guiones) dentro de
`src/main/java/<groupId en carpetas>/`. Con los datos del enunciado
(`com.javaesencial` + `biblioteca-spring`) esa clase se llama
`BibliotecaSpringApplication.java`, en el paquete `com.javaesencial.biblioteca`. Es la
única clase del proyecto recién generado (sin contar la de test).

`mvnw`/`mvnw.cmd` son el *Maven Wrapper*: permiten ejecutar `./mvnw ...` sin tener Maven
instalado en el sistema, descargando la versión exacta que el proyecto necesita.

### 2. El `pom.xml` generado

El bloque `<parent>` fija la versión de Spring Boot que gobierna todo el proyecto:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
    <relativePath/>
</parent>
```

(La versión exacta que ofrece Initializr como "más reciente estable" cambia con el
tiempo — lo importante para responder la pregunta es identificar el número de versión
del `<parent>`, no memorizar uno concreto. El proyecto real de este libro, que arranca en
el Capítulo 2, usa Spring Boot **3.3.0**, y es la referencia que se sigue durante todo el
volumen.)

Con solo *Spring Web* marcada en Initializr, las dependencias que aparecen son dos —una
de producción y otra de test, esta última añadida siempre por defecto aunque no se pida
explícitamente—:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Ninguna de las dos lleva `<version>`: la hereda del `<parent>`. `spring-boot-starter-web`
trae Tomcat embebido, Spring MVC y Jackson (para JSON); `spring-boot-starter-test` trae
JUnit 5, Mockito y `spring-boot-test`, y solo se usa en `src/test/`.

### 3. `src/main/resources/`

Sí existe, y contiene un único archivo: `application.properties`, generado **vacío**
(sin ninguna propiedad). Es el sitio donde a partir del Capítulo 2 se irán añadiendo las
propiedades de configuración del proyecto (puerto, base de datos, logging, etc.). También
se generan las carpetas `static/` y `templates/` (para servir contenido web estático o
plantillas de servidor), vacías, porque este proyecto va a ser una API REST y no las va a
necesitar.

### 4. La clase principal

```java
package com.javaesencial.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BibliotecaSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaSpringApplication.class, args);
    }
}
```

Lleva la anotación `@SpringBootApplication`, que en realidad combina tres anotaciones en
una: `@Configuration` (esta clase puede declarar beans), `@EnableAutoConfiguration`
(activa la autoconfiguración de Spring Boot, que decide qué configurar según lo que haya
en el classpath — por ejemplo, al ver `spring-boot-starter-web` configura Tomcat
automáticamente) y `@ComponentScan` (Spring escanea este paquete y sus subpaquetes en
busca de componentes anotados con `@Component`, `@Service`, `@Repository`,
`@RestController`, etc., y los registra como beans).

`SpringApplication.run(BibliotecaSpringApplication.class, args)` es lo que realmente
arranca la aplicación: crea el contexto de Spring, ejecuta el `ComponentScan`, aplica la
autoconfiguración, levanta el servidor Tomcat embebido en el puerto por defecto (8080) y
deja la aplicación escuchando peticiones HTTP. `args` reenvía los argumentos de línea de
comandos con los que se haya lanzado el JAR, por si la aplicación los necesita.

**Nota:** el nombre exacto de la clase principal (`BibliotecaSpringApplication`) lo
deriva Initializr del artefacto (`biblioteca-spring` → `BibliotecaSpringApplication`).
El proyecto del libro, a partir del Capítulo 2, la renombra a `BibliotecaApplication`
(más corto); es el mismo mecanismo, solo cambia el nombre de la clase.
