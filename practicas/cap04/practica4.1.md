# Práctica 4.1 — Añadir un mensaje de bienvenida a la configuración

## Enunciado

`InfoController` devuelve directamente el bean `BibliotecaConfig` como JSON, así que
cualquier campo nuevo que añadas a esa clase aparece automáticamente en la respuesta de
`/info`, sin tocar el controlador. Añade un mensaje de bienvenida configurable:

1. Añade la propiedad `biblioteca.mensaje-bienvenida=Bienvenido a la Biblioteca
   Municipal` a `application-dev.properties` y a `application-prod.properties`.
2. Añade el campo `mensajeBienvenida` a `BibliotecaConfig` con su getter y setter.
3. Reinicia y comprueba que `GET /info` ya incluye `"mensajeBienvenida"` en el JSON, sin
   haber modificado `InfoController`.
4. Cambia el valor de la propiedad en el fichero y reinicia. ¿El endpoint devuelve el
   nuevo mensaje sin tocar ningún código Java?

## Solución

### 1. Propiedad en los dos ficheros de perfil

`application-dev.properties`:

```properties
server.port=8080
logging.level.com.javaesencial.biblioteca=DEBUG

biblioteca.nombre=Biblioteca Municipal "El Quijote"
biblioteca.version=2.0
biblioteca.max-libros=500
biblioteca.modo-mantenimiento=false
biblioteca.mensaje-bienvenida=Bienvenido a la Biblioteca Municipal
```

`application-prod.properties`:

```properties
server.port=${PORT:8080}
logging.level.com.javaesencial.biblioteca=WARN

biblioteca.nombre=Biblioteca Municipal "El Quijote"
biblioteca.version=2.0
biblioteca.max-libros=500
biblioteca.modo-mantenimiento=false
biblioteca.mensaje-bienvenida=Bienvenido a la Biblioteca Municipal
```

### 2. `BibliotecaConfig` con el campo nuevo

```java
package com.javaesencial.biblioteca.configuracion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "biblioteca")
public class BibliotecaConfig {

    private String nombre;
    private String version;
    private int maxLibros;
    private boolean modoMantenimiento;
    private String mensajeBienvenida;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMaxLibros() {
        return maxLibros;
    }

    public void setMaxLibros(int maxLibros) {
        this.maxLibros = maxLibros;
    }

    public boolean isModoMantenimiento() {
        return modoMantenimiento;
    }

    public void setModoMantenimiento(boolean modoMantenimiento) {
        this.modoMantenimiento = modoMantenimiento;
    }

    public String getMensajeBienvenida() {
        return mensajeBienvenida;
    }

    public void setMensajeBienvenida(String mensajeBienvenida) {
        this.mensajeBienvenida = mensajeBienvenida;
    }
}
```

`InfoController` **no se toca** — sigue exactamente igual que al terminar la sección 4.4:

```java
@RestController
public class InfoController {

    private final BibliotecaConfig config;

    public InfoController(BibliotecaConfig config) {
        this.config = config;
    }

    @GetMapping("/info")
    public BibliotecaConfig info() {
        return config;
    }
}
```

### 3. `GET /info` con el campo nuevo

```json
{
  "nombre": "Biblioteca Municipal \"El Quijote\"",
  "version": "2.0",
  "maxLibros": 500,
  "modoMantenimiento": false,
  "mensajeBienvenida": "Bienvenido a la Biblioteca Municipal"
}
```

`"mensajeBienvenida"` aparece en el JSON sin que `InfoController` sepa nada de su
existencia: como el endpoint devuelve el propio bean `config`, Jackson lo serializa
recorriendo **todos** sus getters —el nuevo `getMensajeBienvenida()` incluido—, no una
lista fija de campos que el controlador tuviera que enumerar.

### 4. Cambiar el valor de la propiedad

Cambiando, por ejemplo, `biblioteca.mensaje-bienvenida=Bienvenido a la Biblioteca
Municipal "El Quijote"` en `application-dev.properties` y reiniciando, `GET /info`
devuelve el nuevo texto sin haber tocado ni una línea de código Java. Es la misma
demostración que la Práctica 2.1 con el puerto, pero ahora con un `@ConfigurationProperties`
tipado en vez de una propiedad suelta: Spring vuelve a leer el fichero al arrancar,
`BibliotecaConfig` recibe el valor nuevo a través de su setter, y `InfoController` lo
sirve sin ningún cambio — es exactamente la ventaja de externalizar la configuración que
ya se razonó en la Práctica 2.1, punto 4.
