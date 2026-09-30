# Fast Food - Sistema Web de Gestión de Pedidos y Ventas

Proyecto académico grupal. El avance actual contiene el CRUD de productos, usuarios y pedidos, y los
conceptos estudiados durante las semanas 2, 3, 6, 7 y 8.

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

## Semana 8

- Seguridad mediante Spring Security.
- Inicio de sesión mediante `POST /api/auth/login`.
- Autenticación JWT con tokens Bearer firmados y con expiración.
- Contraseñas protegidas con BCrypt.
- Autorización por roles `CLIENTE`, `EMPLEADO` y `ADMINISTRADOR`.
- Respuestas estandarizadas `401 Unauthorized` y `403 Forbidden`.
- Un cliente solo puede consultar sus propios pedidos y pagos.
- Pruebas automáticas de autenticación, roles y propiedad de pedidos.

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
se realiza mediante un endpoint administrativo protegido con Spring Security.
Los visitantes no se almacenan en la base de datos.

Un usuario con rol `CLIENTE` puede crear todos los pedidos que desee. Cada pedido
se guarda inicialmente con estado `PENDIENTE` y todavía no descuenta stock. Al
registrar su pago simulado, el sistema comprueba el monto y la disponibilidad de
todos los productos. Si todo es correcto, guarda el pago como `APROBADO`, cambia
el pedido a `CONFIRMADO` y descuenta el stock dentro de una misma transacción. Si
falta stock, responde `409 Conflict` y no guarda ningún cambio.

## Estructura

```text
Cliente HTTP
    -> Filtro JWT de Spring Security
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

Para crear o actualizar automáticamente el administrador inicial y firmar los
tokens JWT, también se configuran estas variables en `.env`:

```properties
APP_ADMIN_EMAIL=admin@fastfood.com
APP_ADMIN_PASSWORD=una-clave-segura
JWT_SECRET=una-clave-aleatoria-de-32-caracteres-o-mas
JWT_ISSUER=fast-food-api
JWT_EXPIRATION_MINUTES=60
```

En PowerShell se puede generar una clave JWT segura con este comando:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

El archivo `.env` está excluido de Git y nunca debe compartirse ni subirse al
repositorio. Solamente `.env.example`, que no contiene secretos, se mantiene en
GitHub.

Después se inicia el backend:

```powershell
.\mvnw.cmd spring-boot:run
```

La API se ejecuta en `http://localhost:8080`.

En Postman primero se envía el correo y la contraseña a
`POST /api/auth/login`. La respuesta contiene un token que se coloca en
`Authorization > Bearer Token` para consumir los endpoints protegidos. Las
consultas `GET` de productos y categorías, el registro público
`POST /api/usuarios` y el login no requieren autenticación previa.

Permisos principales:

| Rol | Acceso |
|---|---|
| Público | Consultar productos y categorías; registrar un usuario cliente |
| `CLIENTE` | Crear y consultar sus propios pedidos; registrar y consultar sus pagos |
| `EMPLEADO` | Consultar y gestionar pedidos e inventario |
| `ADMINISTRADOR` | Gestionar usuarios, roles, catálogo, pedidos, pagos e inventario |

## Seguridad de las tablas en Supabase (RLS)

El Security Advisor de Supabase detectó inicialmente siete tablas del esquema
`public` sin Row Level Security (RLS): `categorias`, `usuarios`,
`detalles_pedido`, `productos`, `inventarios`, `pagos` y `pedidos`.

RLS protege el acceso realizado mediante la API REST o GraphQL automática de
Supabase. Esta protección es distinta de Spring Security: Spring Security
controla quién puede utilizar los endpoints del backend, mientras que RLS
controla qué filas pueden consultarse directamente mediante la API de
Supabase.

El frontend de este proyecto no debe conectarse directamente a las tablas. El
flujo permitido es:

```text
Frontend -> API Spring Boot -> Spring Security -> JPA/Hibernate -> Supabase PostgreSQL
```

Para activar RLS y evitar el acceso directo con los roles públicos de Supabase,
se ejecuta una sola vez el siguiente código desde el SQL Editor del proyecto:

```sql
ALTER TABLE public.categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalles_pedido ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.inventarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pagos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pedidos ENABLE ROW LEVEL SECURITY;

REVOKE ALL PRIVILEGES
ON ALL TABLES IN SCHEMA public
FROM anon, authenticated;

REVOKE ALL PRIVILEGES
ON ALL SEQUENCES IN SCHEMA public
FROM anon, authenticated;
```

Cada instrucción cumple la siguiente función:

- `ENABLE ROW LEVEL SECURITY` activa la protección por filas en cada tabla.
- El primer `REVOKE` retira a los roles `anon` y `authenticated` el acceso
  directo a las tablas mediante la API de Supabase.
- El segundo `REVOKE` impide que esos roles utilicen directamente las
  secuencias que generan los identificadores de las tablas.

Con esta configuración:

- Nadie puede consultar o modificar las tablas utilizando solamente la clave
  pública de Supabase.
- Los usuarios deben pasar por la API de Spring Boot.
- Spring Security continúa aplicando los roles y permisos del sistema.
- La conexión JDBC del backend continúa funcionando porque utiliza el usuario
  propietario de PostgreSQL configurado mediante variables de entorno, que
  normalmente no queda restringido por RLS.

Las credenciales de PostgreSQL nunca deben colocarse en este archivo ni
publicarse en GitHub. actualmente ese acceso directo no forma parte de la arquitectura.

## Endpoints

| Método | Ruta | Operación |
|---|---|---|
| POST | `/api/auth/login` | Validar credenciales y generar un token JWT |
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
| GET | `/api/pedidos/mios` | Consultar los pedidos del usuario autenticado |
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

Las solicitudes manuales se encuentran en `requests.http`. El archivo incluye
login de administrador y cliente, captura automática del token en VS Code REST
Client y ejemplos de consumo mediante `Authorization: Bearer`.

## Ejecutar las pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas de Service utilizan JUnit y Mockito, y las pruebas de Controller
utilizan MockMvc. Las pruebas de integración verifican login, generación y
validación de JWT, tokens alterados o vencidos, respuestas `401` y `403`, roles,
consultas JPQL, commit y rollback. Durante las pruebas se utiliza una base H2 en
memoria, por lo que no requieren las credenciales de Supabase.
