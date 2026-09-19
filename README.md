# Fast Food - Avance de las semanas 2 y 3

Proyecto académico limitado al módulo de productos y a los conceptos estudiados en clase.

## Semana 2

- Endpoints con `GET`, `POST`, `PUT` y `DELETE`.
- `@RestController`, `@RequestMapping` y anotaciones de cada método HTTP.
- Recepción de identificadores mediante `@PathVariable`.
- Recepción de JSON mediante `@RequestBody`.
- Separación `Controller - Service - Repository`.
- Inyección de dependencias mediante constructor.

## Semana 3

- Ciclo TDD `Red - Green - Refactor`.
- Pruebas unitarias con JUnit.
- Repositorio simulado mediante Mockito.
- Pruebas de la capa Service.
- Pruebas de endpoints con MockMvc.

## Estructura

```text
Cliente HTTP
    -> ProductoController
    -> ProductoService
    -> ProductoRepository
    -> H2
```

## Ejecutar la aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

La API se ejecuta en `http://localhost:8080/api/productos`.

## Endpoints

| Método | Ruta | Operación |
|---|---|---|
| GET | `/api/productos` | Listar productos |
| GET | `/api/productos/{id}` | Buscar por identificador |
| POST | `/api/productos` | Registrar un producto |
| PUT | `/api/productos/{id}` | Actualizar un producto |
| DELETE | `/api/productos/{id}` | Eliminar un producto |

Ejemplo de producto:

```json
{
  "nombre": "Hamburguesa clásica",
  "precio": 12.50,
  "stock": 20
}
```

Las solicitudes manuales se encuentran en `requests.http`.

## Ejecutar las pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas de `ProductoService` utilizan JUnit y Mockito. Las pruebas de
`ProductoController` utilizan MockMvc para simular solicitudes HTTP.

El ciclo completo y sus resultados están documentados en `EVIDENCIA-TDD.md`.
