# Fast Food - Sistema Web de Gestión de Pedidos y Ventas

Proyecto académico grupal. El avance actual contiene el CRUD de productos, usuarios y pedidos, y los
conceptos estudiados durante las semanas 2, 3, 6 y 7.

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
- Entidades `Producto`, `Usuario` y `Pedido` asociadas con sus tablas.
- Operaciones CRUD mediante `JpaRepository`.
- Consultas derivadas para usuarios y pedidos.
- PostgreSQL alojado en Supabase.
- H2 utilizado únicamente en las pruebas automáticas.

## Semana 7

- Consultas personalizadas JPQL mediante `@Query` y `@Param`.
- Búsqueda de productos por nombre y por stock disponible.
- Filtro por rango de precios con múltiples condiciones.
- Ordenamiento descendente por precio.
- Consulta sobre la relación `Pedido - DetallePedido - Producto`.
- Función de agregación `SUM` para obtener totales por estado.
- Creación de pedidos pendientes sin modificar el inventario.
- Confirmación mediante un pago simulado con `@Transactional`.
- Descuento de stock al aprobar el pago y rollback automático si no alcanza.

## Modelo de datos del sistema

| Tabla | Propósito |
|---|---|
| `categorias` | Clasificación del menú: hamburguesas, combos, piqueos y bebidas |
| `productos` | Nombre, descripción, precio, categoría y stock disponible |
| `usuarios` | Cuenta, correo, contraseña BCrypt, teléfono, dirección, rol y estado |
| `pedidos` | Usuario cliente, dirección, teléfono, estado, subtotal, costo de entrega, total y fechas |
| `detalles_pedido` | Productos, cantidades, precios unitarios y subtotales del pedido |
| `pagos` | Pago simulado, método, estado, monto y fecha; solo puede existir uno por pedido |
| `inventarios` | Stock mínimo configurado para cada producto |

Las categorías `HAMBURGUESAS`, `COMBOS`, `PIQUEOS` y `BEBIDAS` se crean
automáticamente si todavía no existen. El stock actual permanece en
`productos`; `inventarios` guarda el límite mínimo utilizado para generar
alertas.

La tabla `clientes` fue unificada con `usuarios`. Todo registro público recibe
automáticamente el rol `CLIENTE`, aunque el JSON intente enviar otro rol. Los
roles permitidos son `CLIENTE`, `EMPLEADO` y `ADMINISTRADOR`; el cambio de rol
se realiza mediante un endpoint separado que será protegido con Spring
Security en la siguiente etapa. Los visitantes no se almacenan en la base de
datos.

Un usuario con rol `CLIENTE` puede crear todos los pedidos que desee. Cada pedido
se guarda inicialmente con estado `PENDIENTE` y todavía no descuenta stock. Al
registrar su pago simulado, el sistema comprueba el monto y la disponibilidad de
todos los productos. Si todo es correcto, guarda el pago como `APROBADO`, cambia
el pedido a `CONFIRMADO` y descuenta el stock dentro de una misma transacción. Si
falta stock, responde `409 Conflict` y no guarda ningún cambio.

## Estructura

```text
Cliente HTTP
    -> Controller
    -> Service
    -> Repository
    -> JPQL / Spring Data JPA
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

La API se ejecuta en `http://localhost:8080`.

## Endpoints

| Método | Ruta | Operación |
|---|---|---|
| GET | `/api/productos` | Listar productos |
| GET | `/api/productos/{id}` | Buscar por identificador |
| POST | `/api/productos` | Registrar un producto |
| PUT | `/api/productos/{id}` | Actualizar un producto |
| DELETE | `/api/productos/{id}` | Eliminar un producto |
| GET | `/api/productos/buscar?nombre=...` | JPQL: buscar por nombre |
| GET | `/api/productos/disponibles` | JPQL: buscar con stock |
| GET | `/api/productos/rango-precios?minimo=...&maximo=...` | JPQL: filtrar por rango y stock |
| GET | `/api/productos/ordenados-precio` | JPQL: ordenar por precio |
| PUT | `/api/productos/{id}/descontar-stock?cantidad=...` | Actualización transaccional de stock |
| GET | `/api/pedidos` | Listar pedidos |
| GET | `/api/pedidos/{id}` | Buscar pedido por identificador |
| POST | `/api/pedidos` | Registrar un pedido |
| PUT | `/api/pedidos/{id}` | Actualizar un pedido |
| DELETE | `/api/pedidos/{id}` | Eliminar un pedido |
| GET | `/api/pedidos/usuario/{usuarioId}` | Buscar pedidos por usuario |
| GET | `/api/pedidos/estado/{estado}` | Buscar pedidos por estado |
| POST | `/api/pedidos/procesar` | Crear un pedido pendiente con sus productos, sin descontar stock |
| GET | `/api/pedidos/{id}/detalles` | JPQL: consultar relación con detalles |
| GET | `/api/pedidos/total-por-estado/{estado}` | JPQL: sumar total por estado |
| GET/POST | `/api/categorias` | Listar o registrar categorías |
| GET | `/api/categorias/activas` | Listar categorías disponibles |
| GET/PUT/DELETE | `/api/categorias/{id}` | Consultar, actualizar o eliminar una categoría |
| GET/POST | `/api/usuarios` | Listar o registrar usuarios |
| GET/PUT/DELETE | `/api/usuarios/{id}` | Consultar, actualizar o eliminar un usuario |
| PUT | `/api/usuarios/{id}/rol` | Cambiar el rol desde la operación administrativa |
| GET | `/api/usuarios/correo?correo=...` | Buscar usuario por correo |
| GET | `/api/usuarios/rol/{rol}` | Buscar usuarios por rol |
| GET/POST | `/api/pagos` | Listar pagos o confirmar un pedido mediante un pago simulado |
| GET/PUT/DELETE | `/api/pagos/{id}` | Consultar, actualizar o eliminar un pago |
| GET | `/api/pagos/pedido/{pedidoId}` | Consultar el pago de un pedido |
| GET/POST | `/api/inventarios` | Listar o configurar inventarios |
| GET/PUT/DELETE | `/api/inventarios/{id}` | Consultar, actualizar o eliminar una configuración |
| GET | `/api/inventarios/producto/{productoId}` | Consultar inventario por producto |
| GET | `/api/inventarios/bajo-minimo` | Listar productos cuyo stock llegó al mínimo |

Ejemplo de producto:

```json
{
  "nombre": "Hamburguesa clásica",
  "descripcion": "Hamburguesa de carne con queso y verduras",
  "precio": 12.50,
  "stock": 20,
  "categoria": "HAMBURGUESAS"
}
```

Las solicitudes manuales se encuentran en `requests.http`.

## Ejecutar las pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas de Service utilizan JUnit y Mockito, y las pruebas de Controller
utilizan MockMvc. Las pruebas de integración ejecutan las consultas JPQL y
comprueban tanto el commit como el rollback de la confirmación y el descuento
de stock. Durante las pruebas
se utiliza una base H2 en memoria, por lo que no requieren las credenciales de
Supabase.
