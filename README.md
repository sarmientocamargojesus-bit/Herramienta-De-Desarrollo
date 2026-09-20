# Fast Food - Sistema Web de Gestión de Pedidos y Ventas

Proyecto académico grupal. El avance actual contiene el CRUD de productos y los
conceptos estudiados durante las semanas 2, 3 y 6.

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

## Semana 6

- Persistencia mediante Spring Data JPA e Hibernate.
- Entidad `Producto` asociada con la tabla `productos`.
- Operaciones CRUD mediante `JpaRepository`.
- PostgreSQL alojado en Supabase.
- H2 utilizado únicamente en las pruebas automáticas.

## Estructura

```text
Cliente HTTP
    -> ProductoController
    -> ProductoService
    -> ProductoRepository
    -> Hibernate
    -> Session Pooler de Supabase (puerto 5432)
    -> PostgreSQL
```

## Ejecutar la aplicación

Cada integrante debe obtener en Supabase la conexión `Session pooler` del
puerto `5432`. Después debe copiar la plantilla `.env.example` con el nombre
`.env`:

```powershell
Copy-Item .env.example .env
```

En `.env` se reemplazan los tres valores de ejemplo con el host, usuario y
contraseña reales. Los datos exactos deben copiarse desde la opción `Connect`
del proyecto de Supabase. Spring Boot importa automáticamente este archivo al
iniciar la aplicación.

El archivo `.env` está excluido de Git y nunca debe compartirse ni subirse al
repositorio. Solamente `.env.example`, que no contiene secretos, se mantiene en
GitHub.

Después se inicia el backend:

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
`ProductoController` utilizan MockMvc para simular solicitudes HTTP. Durante
las pruebas se utiliza una base H2 en memoria, por lo que no requieren las
credenciales de Supabase.
