# Práctica 1.2 — Leer el `pom.xml` generado

## Enunciado

Abre el `pom.xml` del proyecto que generaste en la Práctica 1.1 e identifica:

1. El bloque `<parent>`: ¿qué `groupId`, `artifactId` y `version` tiene?
2. Las `<dependencies>`: ¿qué *starters* aparecen? ¿Alguno tiene `<scope>test`? ¿Por qué?
3. El bloque `<build>`: ¿qué plugin aparece? ¿Tiene configuración adicional?
4. En el Volumen 1 usabas `maven-assembly-plugin`. ¿Qué ventaja tiene
   `spring-boot-maven-plugin` sobre él?

Escribe tus respuestas como comentarios en el propio `pom.xml` para tenerlas de
referencia.

## Solución

El `pom.xml` completo que genera Initializr con la configuración de la Práctica 1.1 es
este (es, de hecho, el mismo `pom.xml` con el que arranca el proyecto del libro en el
Capítulo 2):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.javaesencial</groupId>
    <artifactId>biblioteca-spring</artifactId>
    <version>1.0.0</version>
    <name>biblioteca-spring</name>
    <description>Proyecto Biblioteca -- Java Esencial, Volumen 2 (Spring Boot)</description>

    <properties>
        <java.version>21</java.version>
    </properties>

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

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### 1. El bloque `<parent>`

```xml
<!-- groupId: org.springframework.boot -->
<!-- artifactId: spring-boot-starter-parent -->
<!-- version: 3.3.0 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
    <relativePath/>
</parent>
```

`groupId` = `org.springframework.boot`, `artifactId` = `spring-boot-starter-parent`,
`version` = `3.3.0`.

### 2. Las `<dependencies>`

```xml
<!-- Starters: -->
<!--   spring-boot-starter-web   -> sin scope: dependencia de PRODUCCION -->
<!--   spring-boot-starter-test  -> scope=test: solo compila/se usa en src/test/ -->
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

Aparecen dos *starters*: `spring-boot-starter-web` (la única que se marcó explícitamente
en Initializr; trae Tomcat, Spring MVC y Jackson) y `spring-boot-starter-test` (la añade
Initializr por defecto siempre, se haya pedido o no). Solo esta segunda tiene
`<scope>test</scope>`: ese *scope* le dice a Maven que esa dependencia (y todo lo que
trae — JUnit 5, Mockito, AssertJ, `spring-boot-test`) solo está disponible al compilar y
ejecutar el código de `src/test/java`, no al compilar el código de producción de
`src/main/java` ni al empaquetar el JAR final. Es la forma de evitar que las librerías de
test viajen dentro de la aplicación que se despliega.

### 3. El bloque `<build>`

```xml
<!-- Plugin: spring-boot-maven-plugin -->
<!-- Configuracion adicional: ninguna (bloque <plugin> vacio, sin <configuration>) -->
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

Aparece un único plugin, `spring-boot-maven-plugin`, sin ningún `<configuration>`
adicional (la configuración por defecto es la que se usa durante todo este volumen: no
hace falta tocarla hasta que en capítulos más avanzados se necesite, por ejemplo, excluir
alguna dependencia del JAR final).

### 4. `spring-boot-maven-plugin` frente a `maven-assembly-plugin`

```xml
<!-- Volumen 1 (maven-assembly-plugin): -->
<!--   - exige un descriptor (jar-with-dependencies) para saber que empaquetar -->
<!--   - genera un JAR "gordo" plano: mezcla las .class del proyecto y las de -->
<!--     las dependencias en el mismo nivel, sin gestionar colisiones -->
<!--   - no sabe nada de Spring Boot: no genera un MANIFEST que sepa arrancar -->
<!--     un contexto de Spring ni un servidor embebido -->
<!-- Volumen 2 (spring-boot-maven-plugin): -->
<!--   - no necesita descriptor: sabe por convencion que debe empaquetar todo -->
<!--     lo necesario para ejecutar la aplicacion -->
<!--   - genera un "fat JAR" con una estructura propia (BOOT-INF/classes, -->
<!--     BOOT-INF/lib) que SI evita colisiones entre dependencias -->
<!--   - añade un MANIFEST.MF que permite arrancar con "java -jar" sin -->
<!--     necesidad de indicar la clase principal a mano (-cp o -jar clasico) -->
<!--   - añade el objetivo "spring-boot:run", que compila y arranca la app -->
<!--     directamente sin generar el JAR, muy util en desarrollo -->
```

En el Volumen 1, `maven-assembly-plugin` necesitaba un descriptor explícito
(`jar-with-dependencies`) para saber qué incluir, y el resultado era un JAR "plano" que
mezclaba las clases del proyecto con las de todas las dependencias en el mismo nivel del
classpath — funcionaba porque el Volumen 1 no tenía dependencias externas con las que
pudiera haber colisiones de nombres de fichero.

`spring-boot-maven-plugin` no necesita descriptor: por convención, sabe que debe empaquetar
la aplicación como un JAR ejecutable. Pero además hace dos cosas que
`maven-assembly-plugin` no hace: genera un *fat JAR* con una estructura propia
(`BOOT-INF/classes` para las clases del proyecto, `BOOT-INF/lib` para las dependencias,
cada una en su propio JAR interno) que evita las colisiones que un JAR "plano" sí puede
tener, y añade al `MANIFEST.MF` la información necesaria para que `java -jar
biblioteca-spring-1.0.0.jar` arranque la aplicación directamente, sin tener que indicar a
mano cuál es la clase con `main`. Añade también el objetivo `spring-boot:run`, que ni
siquiera genera el JAR: compila y arranca la aplicación directamente, útil en desarrollo
para no esperar al empaquetado completo en cada prueba.
