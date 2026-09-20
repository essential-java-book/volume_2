# Práctica 2.4 — Controlador de información de la biblioteca

## Enunciado

Crea la clase `BibliotecaInfoController` con un endpoint `GET /info` que devuelva
información contextual:

```java
@GetMapping("/info")
public String info() {
    var hora = LocalTime.now().getHour();
    var turno = hora < 14 ? "mañana" : (hora < 20 ? "tarde" : "noche");
    return "Biblioteca Municipal · Turno de " + turno;
}
```

1. Prueba el endpoint a distintas horas del día (o cambia artificialmente el valor de
   `hora` para simular distintos turnos).
2. Extrae los umbrales `14` y `20` a constantes `final` con nombres descriptivos.
3. Añade también el número de libros disponibles (por ahora, un número fijo) al mensaje
   devuelto.

## Solución

La clase completa, con los tres apartados ya aplicados, en un fichero nuevo
`BibliotecaInfoController.java`:

```java
package com.javaesencial.biblioteca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@RestController
public class BibliotecaInfoController {

    private static final int HORA_FIN_MANANA = 14;
    private static final int HORA_FIN_TARDE = 20;
    private static final int LIBROS_DISPONIBLES = 3;

    @GetMapping("/info")
    public String info() {
        return construirMensaje(LocalTime.now().getHour());
    }

    String construirMensaje(int hora) {
        var turno = hora < HORA_FIN_MANANA ? "mañana" : (hora < HORA_FIN_TARDE ? "tarde" : "noche");
        return "Biblioteca Municipal · Turno de " + turno
                + " · " + LIBROS_DISPONIBLES + " libros disponibles";
    }
}
```

No hay que registrar la clase en ningún sitio. Está en el paquete de
`BibliotecaApplication`, así que el escaneo de componentes de `@SpringBootApplication`
la encuentra por llevar `@RestController`, igual que encontró `HolaMundoController`.

### 1. Probar los distintos turnos

```http
GET http://localhost:8080/info
```

Respuesta a las diez de la mañana:

```text
Biblioteca Municipal · Turno de mañana · 3 libros disponibles
```

Para no esperar a que cambie la hora, la lógica está separada en
`construirMensaje(int hora)`, que recibe la hora como parámetro. Así se puede simular
cualquier turno sin tocar el reloj: basta con sustituir temporalmente la llamada por
`construirMensaje(21)` y reiniciar. Los resultados para las horas que marcan las
fronteras:

| Hora | Mensaje |
|---|---|
| 0, 9, 13 | `Biblioteca Municipal · Turno de mañana · 3 libros disponibles` |
| 14, 19 | `Biblioteca Municipal · Turno de tarde · 3 libros disponibles` |
| 20, 23 | `Biblioteca Municipal · Turno de noche · 3 libros disponibles` |

Fíjate en las fronteras: las 14:00 ya son «tarde» y las 20:00 ya son «noche», porque las
comparaciones son `<` estrictas. Y un detalle que delata que la regla es demasiado
simple: a las 3 de la madrugada el mensaje dice «mañana». Para una biblioteca que a esa
hora está cerrada no importa, pero es el tipo de caso límite que conviene probar siempre.

Separar el método tiene una segunda ventaja que aprovecharás en el Capítulo 8: un método
que recibe la hora como parámetro se puede comprobar con un test, mientras que uno que
llama a `LocalTime.now()` por dentro da un resultado distinto según cuándo se ejecute.

### 2. Los umbrales como constantes

```java
private static final int HORA_FIN_MANANA = 14;
private static final int HORA_FIN_TARDE = 20;
```

Un `14` suelto en mitad de una expresión es un «número mágico»: quien lo lee tiene que
adivinar qué significa. Con nombre, la condición se lee casi como una frase
(`hora < HORA_FIN_MANANA`) y, si la biblioteca cambia de horario, el cambio se hace en un
único sitio. Son `static` porque el valor es el mismo para toda la clase, `final` porque
no cambia y `private` porque a nadie de fuera le hace falta; el nombre en mayúsculas con
guiones bajos es la convención de Java para constantes, la misma del Volumen 1.

Los nombres van sin `ñ` (`MANANA`, no `MAÑANA`) a propósito. Java lo admite, pero un
identificador con caracteres no ASCII da problemas en cuanto el fichero pasa por un
editor o una terminal con otra codificación. Dentro de las cadenas de texto no hay ese
riesgo, y por eso el mensaje sí dice «mañana».

### 3. El número de libros en el mensaje

```java
private static final int LIBROS_DISPONIBLES = 3;
```

y se concatena al final del mensaje, como se ve en la clase completa. Es un valor fijo
porque en este punto del libro todavía no existe nada a lo que preguntárselo. Deja de
serlo enseguida: en el Capítulo 3 aparece `BibliotecaService` y el controlador podrá
recibirlo por el constructor y pedirle el número real de libros, y en el Capítulo 4 el
nombre de la biblioteca sale del código y pasa a `application.properties`.

> **Aviso para más adelante.** Esta clase es solo de la práctica: no forma parte del
> proyecto del libro ni está en los tags del repositorio. En el Capítulo 4 el proyecto
> crea su propio `InfoController`, también en `GET /info`. Si para entonces conservas
> `BibliotecaInfoController`, la aplicación no arrancará y el log dirá
> `Ambiguous mapping`, porque dos métodos no pueden atender la misma ruta. Borra esta
> clase (o cambia su ruta a `/info-turno`) antes de empezar el Capítulo 4.
