# Problemas encontrados en Fast Food

**Fecha de revisión:** 28 de octubre de 2026 (America/Lima).  
**Proyecto:** repositorio principal Herramienta-De-Desarrollo.  
**Commit revisado:** `16ae97b`.  
**Estado de los hallazgos:** pendientes de decisión y corrección al registrar esta revisión.

Este documento reúne los problemas encontrados en el backend Spring Boot, el frontend Angular y la configuración de inventario de Fast Food. Su propósito es dejar constancia de lo comprobado y de los cambios que se pueden evaluar. Las propuestas todavía no se han aplicado al código.

## Resultado general

El flujo principal funciona: se pudo registrar un cliente, iniciar sesión, conservar el carrito del visitante, importarlo a la cuenta, confirmar una compra simulada, consultar el pedido y descontar el stock. Ambos proyectos compilaron correctamente. Sin embargo, las comprobaciones adicionales reprodujeron los problemas descritos en este documento.

Las operaciones que crearon, modificaron o eliminaron datos se realizaron en una copia temporal del código con H2 y cuentas ficticias. La base de datos de Supabase se consultó mediante una transacción de solo lectura. Los datos reales y el código de la aplicación quedaron intactos durante la revisión.

## Pruebas realizadas

| Comprobación | Resultado |
| --- | --- |
| Backend: `mvnw.cmd -o test` | 57 pruebas correctas, sin fallos, errores ni pruebas omitidas. |
| Frontend: `npm test -- --watch=false` | 39 pruebas correctas en 36 archivos. |
| Angular: `npm run build` | Compilación de producción correcta. |
| Arranque del backend y de Angular | Correcto en los puertos temporales de revisión, con proxy hacia la API. |
| Recorrido desde el navegador | Registro, login, carrito, compra simulada, detalle del pedido y edición de producto comprobados. |
| Comprobaciones HTTP iniciales | 25 resultados esperados correctos; otros 4 casos de diagnóstico reprodujeron problemas. |
| Dos checkouts simultáneos con stock 1 | Uno respondió 200 y el otro 409. El stock final fue 0. |
| Dos pagos simultáneos por el endpoint antiguo con stock 1 | Ambos respondieron 201. Se aprobó la venta de dos unidades aunque solo había una. |
| Prueba temporal de dos clics rápidos en Agregar | Falló: se esperaba cantidad 2 y se obtuvo 1. Esta prueba no se agregó al repositorio principal. |
| Conexión PostgreSQL/Supabase | Correcta y verificada en modo de solo lectura. |

Las 57 pruebas del backend y las 39 del frontend corresponden a las suites existentes. Que pasen no significa que cubran todos los recorridos: varios problemas se encontraron mediante comprobaciones adicionales. No se realizó una prueba de carga, un análisis externo de vulnerabilidades de dependencias ni una certificación para todos los navegadores.

## Problemas de prioridad alta

### 1. El registro público puede sobrescribir una cuenta existente

**Cómo se comprobó:** se envió a `POST /api/usuarios` el ID de una cuenta ficticia existente junto con un correo nuevo. La API respondió 201 y guardó los datos sobre esa misma fila.

**Qué ocurre:** el registro recibe una entidad `Usuario` y conserva el identificador enviado antes de llamar a `save`. Esto permite sustituir datos de una cuenta sin utilizar el endpoint administrativo, incluida su contraseña. El servicio también asigna el rol CLIENTE.

**Cambio propuesto:** recibir un DTO de registro sin ID, crear una entidad nueva en el servidor y rechazar o ignorar identificadores de entrada. Incorporar una prueba que compruebe que registrar una cuenta nunca actualiza otra.

**Referencia:** [UsuarioService.java](src/main/java/com/fastfood/fast_food_backend/services/UsuarioService.java#L49).

### 2. El endpoint antiguo permite confirmar pedidos sin productos ni pago

**Cómo se comprobó:** un cliente autenticado envió un pedido con estado `CONFIRMADO` y un total elegido en el cuerpo de `POST /api/pedidos`. La API respondió 201. El pedido no tenía detalles y la consulta de su pago devolvió 404.

**Qué ocurre:** Angular utiliza el checkout nuevo, pero el endpoint anterior sigue disponible y admite valores que evitan el flujo de confirmación y stock. Por revisión del código, el `PUT` de pedidos también permite cambiar estados e importes sin recalcularlos a partir de sus detalles.

**Cambio propuesto:** mantener la compatibilidad necesaria del APF2 y hacer que el servidor controle estados y totales. Confirmar un pedido debe exigir productos válidos y pasar por la operación de pago y actualización de stock correspondiente.

**Referencia:** [PedidoService.java](src/main/java/com/fastfood/fast_food_backend/services/PedidoService.java#L51).

### 3. El pago antiguo puede vender dos veces la última unidad

**Cómo se comprobó:** se crearon dos pedidos pendientes para un producto con stock 1. Al enviar sus pagos simultáneamente a `POST /api/pagos`, ambos respondieron 201 y quedaron aprobados. El stock terminó en 0 aunque se vendieron dos unidades.

**Qué ocurre:** `PagoService` comprueba y descuenta el stock sin el bloqueo de producto que utiliza el checkout nuevo. Tener una transacción no impidió que las dos solicitudes leyeran la misma disponibilidad.

**Cambio propuesto:** compartir una operación transaccional de confirmación entre ambos caminos, con bloqueo de productos y del pedido. Conservar la validación final de stock. El checkout nuevo sí rechazó la segunda compra en la prueba simultánea.

**Referencias:** [PagoService.java](src/main/java/com/fastfood/fast_food_backend/services/PagoService.java#L60) y [CheckoutService.java](src/main/java/com/fastfood/fast_food_backend/services/CheckoutService.java#L65).

## Problemas de prioridad media

### 4. Cambiar de domicilio a recojo puede bloquear el checkout

**Cómo se comprobó:** en el navegador se seleccionó A domicilio, se intentó confirmar sin dirección y después se volvió a Recojo. El formulario siguió mostrando «Para domicilio, escribe una dirección de entrega» y no confirmó, incluso al volver a pulsar el botón. Recargar el checkout permitió completar la compra por recojo.

**Cambio propuesto:** gestionar explícitamente los validadores de dirección cuando cambia el tipo de entrega y actualizar la validez del control. Mostrar el aviso de dirección únicamente cuando corresponda y probar el cambio entre ambas opciones.

**Referencia:** [checkout.ts](front-end/src/app/components/checkout/checkout.ts#L13).

### 5. Renombrar una categoría desincroniza el catálogo

**Cómo se comprobó:** al renombrar una categoría de prueba, el producto mantuvo su nombre de categoría anterior y el mismo `categoriaId`. Al seleccionar el filtro con el nombre nuevo, Angular mostró que no había productos.

**Qué ocurre:** el producto guarda la referencia a la categoría y un texto con su nombre. Cambiar el nombre de la categoría no sincroniza ese texto, que el frontend utiliza para filtrar.

**Cambio propuesto:** obtener el nombre desde la relación al construir la respuesta o sincronizarlo dentro de la misma transacción. Filtrar por ID también evita depender de un nombre que puede cambiar.

**Referencias:** [CategoriaService.java](src/main/java/com/fastfood/fast_food_backend/services/CategoriaService.java#L47) e [inicio.ts](front-end/src/app/components/inicio/inicio.ts).

### 6. Faltan las configuraciones de inventario de los 17 productos existentes

**Cómo se comprobó:** la consulta de solo lectura a Supabase encontró 17 productos y 0 registros en `inventarios`.

**Qué ocurre:** los productos nuevos reciben un inventario automáticamente, pero los productos anteriores no se completan de forma retroactiva. Por eso, esos productos no aparecen en la consulta de inventario y no cuentan con su mínimo ni con el seguimiento de fechas de alerta.

**Cambio propuesto:** definir un mínimo individual para cada producto existente y registrar su inventario una sola vez. El panel podría mostrar los productos pendientes de configuración. Las fechas históricas que nunca se registraron deben conservarse como desconocidas.

**Referencias:** [InventarioService.java](src/main/java/com/fastfood/fast_food_backend/services/InventarioService.java#L63) y [script de inventario](docs/inventario-fechas-supabase.sql).

### 7. Dos clics rápidos pueden perder una unidad del carrito

**Cómo se comprobó:** una prueba temporal del servicio Angular ejecutó dos acciones de Agregar antes de completar la primera consulta. Ambas calcularon cantidad 1 y el carrito terminó con una unidad en vez de dos.

**Cambio propuesto:** serializar las operaciones por producto o implementar un incremento atómico en la API. Un indicador de operación en curso puede evitar clics duplicados accidentales. La comprobación de stock debe permanecer en el backend.

**Referencia:** [carrito.service.ts](front-end/src/app/core/carrito.service.ts#L46).

### 8. Los tokens anteriores conservan permisos retirados

**Cómo se comprobó:** después de cambiar un empleado a CLIENTE, su token anterior todavía permitió consultar inventarios con respuesta 200. En otra cuenta, desactivar al empleado tampoco impidió esa consulta con su token anterior.

**Qué ocurre:** el filtro comprueba la firma y las fechas del JWT y utiliza el rol incluido en él. No comprueba el estado actual de la cuenta. Con la duración predeterminada, el acceso puede continuar hasta que venza el token de 60 minutos.

**Cambio propuesto:** decidir si estos cambios deben invalidar el acceso inmediatamente. Para conseguirlo, verificar cuenta activa y rol vigente o utilizar una versión de sesión que cambie al retirar permisos. La renovación de tokens no resuelve por sí sola este problema.

**Referencia:** [JwtAuthenticationFilter.java](src/main/java/com/fastfood/fast_food_backend/security/JwtAuthenticationFilter.java#L52).

### 9. Los accesos de cuenta desaparecen en pantallas pequeñas

**Cómo se comprobó:** la vista móvil y el SCSS muestran que por debajo de 760 px se oculta `.future-action`. Esa clase contiene los enlaces Iniciar sesión, Mis pedidos y Gestión, según el tipo de cuenta. No hay un menú móvil alternativo para esos accesos, aunque algunos recorridos mantienen enlaces contextuales.

**Cambio propuesto:** conservar un acceso compacto a la cuenta o incluir un menú móvil que muestre todas las opciones permitidas.

**Referencia:** [encabezado.scss](front-end/src/app/components/encabezado/encabezado.scss#L7).

### 10. El regreso del detalle administrativo lleva a una ruta de cliente

**Cómo se comprobó:** desde Gestión → Pedidos → Ver detalle, el enlace Mis pedidos navega a `/mis-pedidos`. Esa ruta solo admite CLIENTE y el administrador termina viendo Acceso denegado.

**Cambio propuesto:** elegir el destino según el contexto: `/gestion/pedidos` para gestión y `/mis-pedidos` para el cliente.

**Referencia:** [detalle-pedido.html](front-end/src/app/components/detalle-pedido/detalle-pedido.html#L1).

### 11. El endpoint antiguo de pagos acepta métodos inexistentes

**Cómo se comprobó:** `POST /api/pagos` guardó un pago APROBADO con el método `METODO_INEXISTENTE`. El checkout nuevo sí limita los métodos a YAPE y TARJETA y exige su referencia de simulación.

**Cambio propuesto:** compartir las reglas de validación y limitar los métodos y estados admitidos en todos los endpoints de pago. Los pagos deben seguir siendo simulados.

**Referencia:** [PagoService.java](src/main/java/com/fastfood/fast_food_backend/services/PagoService.java).

## Problemas de prioridad baja

### 12. El login muestra sesión vencida cuando las credenciales son incorrectas

**Cómo se comprobó:** una contraseña incorrecta produjo 401 en la API, pero Angular mostró un aviso de sesión inválida o vencida. El tratamiento general de errores sustituye cualquier respuesta 401 por ese mensaje.

**Cambio propuesto:** distinguir el login de las solicitudes protegidas y mostrar el aviso correspondiente a correo o contraseña incorrectos.

**Referencia:** [errores.ts](front-end/src/app/core/errores.ts#L6).

### 13. Subir una imagen sin archivo produce una respuesta 500

**Cómo se comprobó:** una solicitud multipart sin la parte obligatoria `archivo` respondió 500.

**Cambio propuesto:** manejar la ausencia del archivo como un error de datos enviados, por ejemplo con respuesta 400 y un mensaje comprensible.

**Referencia:** [ManejadorGlobalExcepciones.java](src/main/java/com/fastfood/fast_food_backend/exceptions/ManejadorGlobalExcepciones.java).

## Estado de Supabase al revisar

| Tabla | Registros |
| --- | ---: |
| usuarios | 2 |
| categorias | 4 |
| productos | 17 |
| inventarios | 0 |
| carrito_items | 0 |
| pedidos | 0 |
| detalles_pedido | 0 |
| pagos | 0 |
| imagenes_producto | 0 |

No se encontraron stocks negativos, nombres de categoría desincronizados ni pedidos confirmados sin pago en los datos actuales de Supabase. Los ejemplos de fallos se generaron únicamente en H2.

Las nueve tablas revisadas tenían RLS activado y no se encontraron permisos directos de tabla para `anon` ni `authenticated`. La aplicación accede a los datos mediante la conexión JDBC del backend. Esta comprobación corresponde a las tablas revisadas; no equivale a una auditoría completa de las funciones y políticas de la plataforma.

## Mejoras opcionales

- **Pruebas de comportamiento:** ampliar los casos de formularios, permisos, errores, carrito y compra. Se encontraron 30 pruebas que únicamente comprueban que el componente se crea.
- **Pruebas de API:** completar los casos de imágenes y exportar una colección de Postman. Actualizar la creación manual de inventario en `requests.http`, ya que un producto nuevo recibe uno automáticamente.
- **README general:** incorporar el frontend, las instrucciones de arranque de ambas aplicaciones, la arquitectura completa y `carrito_items` en el resumen del modelo de datos.
- **Presentación:** mostrar fechas legibles y un total claro antes de confirmar. El checkout actual guarda costo de entrega 0; cambiar esa tarifa requiere definir la regla de negocio.
- **Mantenimiento:** formatear los archivos comprimidos en una línea y evaluar `strict` y `strictTemplates` por etapas.
- **Validaciones:** reforzar espacios en blanco, longitudes y avisos por campo, con reglas equivalentes en frontend y backend.
- **Historial:** decidir si los productos vendidos se desactivarán en lugar de borrarse. El rechazo actual con 409 conserva las relaciones y no es un fallo de integridad.

## Orden sugerido de atención

1. Corregir la sobrescritura de cuentas, la confirmación directa de pedidos y la venta simultánea del pago antiguo.
2. Ajustar el checkout, la sincronización de categorías y las operaciones del carrito.
3. Completar los mínimos de inventario de los productos existentes y definir la invalidación de tokens.
4. Corregir los accesos móviles, el regreso administrativo, los métodos de pago y los mensajes de error.
5. Ampliar las pruebas y actualizar la documentación junto con cada cambio aprobado.

Las propuestas mantienen la idea del proyecto: catálogo administrado, carrito persistente, pagos simulados y confirmación con descuento de stock dentro de una transacción. Este documento registra los hallazgos de la fecha indicada; su corrección deberá comprobarse y documentarse cuando se implemente.

## Seguimiento de las correcciones

Los problemas descritos arriba se conservan como registro de la revisión inicial del 28 de octubre de 2026. Este apartado indica qué se hizo después para resolver cada punto y cuál es su estado actual. Los puntos se atenderán individualmente cuando el usuario lo solicite.

| Punto | Problema | Estado actual |
| --- | --- | --- |
| 1 | Sobrescritura de cuentas desde el registro público | Resuelto y verificado el 28 de octubre de 2026. |
| 2 | Confirmación de pedidos sin productos ni pago | Resuelto y verificado el 28 de octubre de 2026. |
| 3 | Venta simultánea de la última unidad por el pago antiguo | Resuelto y verificado el 1 de noviembre de 2026. |
| 4 | Bloqueo al cambiar de domicilio a recojo | Resuelto y verificado el 1 de noviembre de 2026. |
| 5 | Desincronización al renombrar categorías | Pendiente. |
| 6 | Inventarios pendientes para los productos existentes | Resuelto en el código el 1 de noviembre de 2026; se aplica a Supabase al iniciar el backend. |
| 7 | Pérdida de unidades por clics rápidos en el carrito | Pendiente. |
| 8 | Tokens anteriores conservan permisos retirados | Pendiente. |
| 9 | Accesos de cuenta ocultos en móvil | Pendiente. |
| 10 | Regreso incorrecto del detalle administrativo de pedidos | Resuelto y verificado el 1 de noviembre de 2026. |
| 11 | Métodos de pago inexistentes aceptados | Resuelto y verificado el 1 de noviembre de 2026. |
| 12 | Mensaje de sesión vencida al fallar el login | Resuelto y verificado el 1 de noviembre de 2026. |
| 13 | Respuesta 500 cuando falta el archivo de imagen | Resuelto y verificado el 1 de noviembre de 2026. |
| 14 | Registro de pago con el ID de otro pago (hallazgo del 1 de noviembre) | Resuelto y verificado el 1 de noviembre de 2026. |
| 15 | Creación de productos, categorías e inventarios con el ID de otro registro (hallazgo del 1 de noviembre) | Resuelto y verificado el 1 de noviembre de 2026. |
| 16 | Respuesta 500 por parámetros faltantes, método no permitido o tipo de contenido incorrecto (hallazgo del 1 de noviembre) | Resuelto y verificado el 1 de noviembre de 2026. |

### Punto 1. Registro público de usuarios

**Fecha de corrección:** 28 de octubre de 2026.  
**Estado:** resuelto y verificado.

#### Qué se hizo para corregirlo

1. Se agregó `RegistroUsuarioRequest`, un DTO que recibe los datos permitidos del registro: nombre, correo, contraseña, teléfono, dirección y estado activo. El DTO no tiene campos para el identificador ni para el rol; los campos adicionales enviados en el JSON se ignoran.
2. Se cambió el controlador para recibir ese DTO en `POST /api/usuarios`, en lugar de recibir directamente la entidad JPA.
3. Se cambió el servicio para construir siempre una entidad `Usuario` nueva. El identificador lo genera la base de datos, por lo que el registro ya no puede utilizar el ID de una cuenta existente para actualizarla.
4. Se mantuvieron la asignación automática del rol CLIENTE, las validaciones del registro, el cifrado de contraseña con BCrypt y la respuesta sin contraseña. La respuesta del registro válido sigue siendo 201.
5. Se agregó una prueba de integración de seguridad y se adaptó la prueba existente del servicio al nuevo DTO.

#### Comportamiento después del cambio

Si alguien envía el ID de una cuenta existente junto con los datos válidos de una cuenta nueva, ese ID se ignora. Se crea otra cuenta con un ID diferente y la cuenta anterior conserva su nombre, correo, contraseña, teléfono, dirección, rol y estado activo. Si el correo ya está registrado, se responde 409 y no se modifica la cuenta existente.

El formulario actual de Angular puede seguir enviando sus mismos datos al mismo endpoint. Esta corrección no requiere modificar el esquema de Supabase.

#### Pruebas realizadas

| Caso | Resultado |
| --- | --- |
| Enviar el ID de un CLIENTE existente con un correo nuevo | Se creó otra cuenta y se conservaron todos los datos de la original. |
| Enviar el ID de un ADMINISTRADOR existente con un correo nuevo | Se creó otra cuenta; la cuenta original conservó sus datos y su rol. |
| Enviar además `rol: ADMINISTRADOR` en el registro público | La cuenta nueva recibió el rol CLIENTE. |
| Registrar una cuenta con el cuerpo que utiliza el frontend | Respuesta 201, ID generado, correo normalizado, cuenta activa por defecto y contraseña protegida. |
| Enviar un correo duplicado junto con un ID existente | Respuesta 409, sin modificar la cuenta ni aumentar el número de usuarios. |

La nueva clase de pruebas ejecuta cuatro casos: dos del registro con ID existente mediante una prueba parametrizada, uno del registro normal y uno del correo duplicado. Antes de corregir el código, los dos casos de ID existente fallaron y reprodujeron el problema. Después del cambio, los cuatro pasaron.

También se ejecutó la suite completa del backend del proyecto principal: **61 pruebas, 0 fallos, 0 errores y 0 omitidas**. Se utilizó H2 y se deshabilitó la importación de `.env` durante la ejecución, por lo que las pruebas no modificaron Supabase.

Comando de comprobación, desde la raíz del proyecto:

```powershell
.\mvnw.cmd -o "-Dspring.config.import=" test
```

La opción `-o` utiliza las dependencias ya descargadas en Maven; en un equipo que aún no las tenga, se ejecuta el comando sin esa opción.

#### Archivos involucrados

- [RegistroUsuarioRequest.java](src/main/java/com/fastfood/fast_food_backend/dto/RegistroUsuarioRequest.java).
- [UsuarioController.java](src/main/java/com/fastfood/fast_food_backend/controllers/UsuarioController.java).
- [UsuarioService.java](src/main/java/com/fastfood/fast_food_backend/services/UsuarioService.java).
- [RegistroUsuarioIntegrationTest.java](src/test/java/com/fastfood/fast_food_backend/security/RegistroUsuarioIntegrationTest.java).
- [NuevasEntidadesIntegrationTest.java](src/test/java/com/fastfood/fast_food_backend/services/NuevasEntidadesIntegrationTest.java).

Al cerrar el punto 1, el siguiente punto pendiente era el **2: controlar la confirmación de pedidos**. Su corrección se documenta a continuación.

### Punto 2. Control de creación y edición de pedidos

**Fecha de corrección:** 28 de octubre de 2026.
**Estado:** resuelto y verificado.

#### Qué se hizo para corregirlo

1. Se agregaron `CrearPedidoRequest` y `ActualizarPedidoRequest`. Los controladores reciben los datos permitidos de cada operación en lugar de aceptar una entidad JPA completa.
2. `POST /api/pedidos` ahora exige detalles con identificador de producto y cantidad positiva. Se reutiliza la operación de creación de `/api/pedidos/procesar`, que construye un pedido nuevo con estado PENDIENTE y obtiene los precios desde el catálogo. Se ignoran el ID, el estado, las fechas y los importes enviados en el cuerpo.
3. Ambas rutas calculan subtotal y total en el servidor. El costo de entrega actual es 0, igual que en el checkout que utiliza Angular; el cliente ya no puede elegir ese importe en la solicitud. No se agregó una tarifa nueva.
4. Se validaron los tipos de entrega RECOJO y DOMICILIO y las líneas vacías, nulas o con cantidades inválidas. Un producto inexistente devuelve 404 y revierte la creación del pedido y sus detalles.
5. `PUT /api/pedidos/{id}` quedó limitado a teléfono, tipo de entrega, dirección y observaciones. Conserva cliente, productos, estado, importes, precios históricos y fecha de creación. Los campos adicionales recibidos se ignoran. Se bloquea la fila durante esta edición para actualizar los datos de entrega sobre el pedido vigente.
6. Se adaptó la comprobación de propiedad del pedido al nuevo DTO. Un cliente sigue sin poder crear pedidos para otra cuenta; el administrador y el empleado conservan sus permisos de gestión.
7. Se actualizaron las solicitudes 14 y 18 de `requests.http` y se añadió al README general el cuerpo de creación válido y el alcance de la edición.

#### Comportamiento después del cambio

Un intento de crear un pedido CONFIRMADO sin productos devuelve 400 y no se guarda. Si la solicitud incluye productos válidos y además envía un estado o total inventado, esos campos se ignoran: el servidor crea un pedido PENDIENTE y calcula los importes con los precios del catálogo.

Un ID enviado en la creación tampoco actualiza un pedido existente. El nuevo pedido recibe su propio identificador. Un pedido de otra cuenta conserva su propietario y sus detalles.

La creación de un pedido pendiente no reserva ni descuenta stock. La confirmación se realiza al aprobar el pago simulado de `/api/pagos`, o mediante la operación completa de `/api/checkout`. Ambos caminos de compra pasaron sus comprobaciones funcionales. La corrección de pagos simultáneos del endpoint antiguo sigue correspondiendo al punto 3.

La edición de entrega no confirma un pedido pendiente ni devuelve uno pagado al estado PENDIENTE. Tampoco cambia la compra cuando se actualiza el precio del producto en el catálogo: los detalles conservan su precio unitario histórico. Este PUT no se utiliza para pasar a preparación o entrega.

El frontend actual continúa utilizando `/api/checkout` para la compra y las mismas rutas de consulta. La corrección no requiere un script de modificación del esquema de Supabase.

#### Pruebas realizadas

Se agregó `PedidoIntegridadIntegrationTest`, con 14 casos ejecutados por MockMvc contra la API real de Spring Boot y H2. No hay una transacción de prueba envolviendo las solicitudes: así se comprueba que la transacción del servicio se revierte realmente cuando falla la creación.

| Caso | Resultado |
| --- | --- |
| Pedido CONFIRMADO sin productos | 400 y ningún pedido creado. |
| Estado, total, costo y precios de línea manipulados | Se ignoraron; pedido PENDIENTE, subtotal y total calculados desde el catálogo, costo de entrega 0. |
| ID de un pedido existente enviado en la creación | Se creó un pedido diferente; el original conservó propietario y detalles. |
| Cliente que intenta crear un pedido para otra cuenta | 403 y ningún pedido creado. |
| Detalles vacíos, línea nula o cantidad 0 | Los tres casos respondieron 400 sin crear pedidos. |
| Producto inexistente después de una línea válida | 404; la base de datos no conservó pedido ni detalles parciales. |
| Importes manipulados en `/api/pedidos/procesar` | Pedido PENDIENTE con importes calculados por el servidor. |
| Tipo de entrega inexistente | 400 y ningún pedido creado. |
| Edición con estado, cliente, fecha, importes y productos manipulados | Se conservaron los datos originales; solo se actualizó la observación permitida. |
| Edición parcial de teléfono, entrega y dirección | 200, conservando estado e importes. |
| Edición de un pedido pagado después de cambiar el precio del catálogo | Se conservaron estado CONFIRMADO, importe del pago, precio histórico y stock. |
| Creación por la ruta antigua y posterior pago simulado | Pedido confirmado, pago aprobado y descuento correcto de stock. |

Antes de corregir el código, 13 de los 14 casos fallaron y reprodujeron los problemas; la comprobación de propiedad ya funcionaba. Después del cambio, pasaron los 14. También pasaron las pruebas unitarias y transaccionales existentes de pedidos: **21 comprobaciones centradas en pedidos**.

Finalmente se ejecutó la suite completa del backend principal: **75 pruebas, 0 fallos, 0 errores y 0 omitidas**. Incluye las pruebas de registro del punto 1, seguridad, JWT, checkout, pagos, stock e imágenes. Se utilizó H2 con la importación de `.env` deshabilitada; las pruebas no modificaron los datos de Supabase.

Comando utilizado desde la raíz del proyecto, con las dependencias ya descargadas:

```powershell
.\mvnw.cmd -o "-Dspring.config.import=" test
```

#### Archivos involucrados

- [CrearPedidoRequest.java](src/main/java/com/fastfood/fast_food_backend/dto/CrearPedidoRequest.java).
- [ActualizarPedidoRequest.java](src/main/java/com/fastfood/fast_food_backend/dto/ActualizarPedidoRequest.java).
- [ProcesarPedidoRequest.java](src/main/java/com/fastfood/fast_food_backend/dto/ProcesarPedidoRequest.java).
- [DetallePedidoRequest.java](src/main/java/com/fastfood/fast_food_backend/dto/DetallePedidoRequest.java).
- [PedidoController.java](src/main/java/com/fastfood/fast_food_backend/controllers/PedidoController.java).
- [PedidoService.java](src/main/java/com/fastfood/fast_food_backend/services/PedidoService.java).
- [AutorizacionPedido.java](src/main/java/com/fastfood/fast_food_backend/security/AutorizacionPedido.java).
- [PedidoRepository.java](src/main/java/com/fastfood/fast_food_backend/repositories/PedidoRepository.java).
- [PedidoIntegridadIntegrationTest.java](src/test/java/com/fastfood/fast_food_backend/security/PedidoIntegridadIntegrationTest.java).
- [PedidoServiceTest.java](src/test/java/com/fastfood/fast_food_backend/services/PedidoServiceTest.java).
- [Solicitudes de API](requests.http) y [README general](README.md).

### Punto 3 y punto 14. Pagos simultáneos y pago con ID de otro pago

**Fecha de corrección:** 1 de noviembre de 2026.
**Estado:** resueltos y verificados.

El punto 14 se encontró en una revisión posterior del código. Se corrigió junto con el punto 3 porque ambos están en `PagoService.registrar`.

**Problema del punto 14:** `POST /api/pagos` guardaba la entidad `Pago` recibida tal como llegaba. Si un cliente enviaba el `id` de un pago existente de otra cuenta junto con su propio pedido pendiente, ese pago se movía a su pedido y el pedido original quedaba sin pago. Es el mismo tipo de fallo que el punto 1.

#### Qué se hizo para corregirlo

1. `PagoService.registrar` bloquea primero el pedido con `PedidoRepository.bloquearPorId`. Luego comprueba que no tenga pago y que siga PENDIENTE. Dos pagos del mismo pedido se atienden uno después del otro.
2. Se agregó `DetallePedidoRepository.buscarProductoIdsPorPedidoId`, que obtiene solo los identificadores de los productos del pedido. Con ellos se bloquean los productos en orden de ID, igual que en el checkout, antes de leer el stock. Así, el segundo pago espera al primero y lee el stock ya descontado.
3. El servicio construye siempre un `Pago` nuevo con el pedido bloqueado, el método, el monto validado, el estado APROBADO y la fecha del servidor. Se ignoran el ID, el estado y la fecha enviados en el JSON.
4. No cambian el JSON de `/api/pagos`, el checkout, el frontend ni el esquema de Supabase. El pago sigue siendo simulado. La validación de métodos de pago corresponde al punto 11 y se documenta en su propio apartado.

#### Pruebas realizadas

Se agregó `PagoIntegridadIntegrationTest`, con 7 ejecuciones contra H2 y sin una transacción de prueba envolvente, para que cada pago use su propia transacción como en la API real.

| Caso | Resultado |
| --- | --- |
| Dos pagos simultáneos de pedidos distintos sobre un producto con stock 1 (5 repeticiones) | Uno aprobado y otro rechazado con stock insuficiente; stock final 0, un pago y un pedido CONFIRMADO. |
| Dos pagos simultáneos del mismo pedido | Solo se registra un pago y el stock se descuenta una vez. |
| Pago con el ID de un pago de otra cuenta, más estado y fecha manipulados | Respuesta 201 con un ID nuevo y estado APROBADO; el pago original conserva su pedido. |

Antes de corregir el código, las 5 repeticiones del pago simultáneo fallaron: los dos pagos se aprobaron. El caso del ID ajeno también falló, porque se devolvió y modificó el pago existente. Después del cambio pasaron los 7.

Suite completa del backend: **82 pruebas, 0 fallos, 0 errores y 0 omitidas**, con H2 y la importación de `.env` deshabilitada.

```powershell
.\mvnw.cmd -o "-Dspring.config.import=" test
```

#### Archivos involucrados

- [PagoService.java](src/main/java/com/fastfood/fast_food_backend/services/PagoService.java).
- [DetallePedidoRepository.java](src/main/java/com/fastfood/fast_food_backend/repositories/DetallePedidoRepository.java).
- [PagoIntegridadIntegrationTest.java](src/test/java/com/fastfood/fast_food_backend/security/PagoIntegridadIntegrationTest.java).

### Punto 11. Métodos y estados de pago inexistentes

**Fecha de corrección:** 1 de noviembre de 2026.
**Estado:** resuelto y verificado.

#### Qué se hizo para corregirlo

1. `PagoService` aplica la misma regla que el checkout: el pago simulado solo admite `YAPE` o `TARJETA`. El método se normaliza sin importar mayúsculas ni espacios. Cualquier otro valor responde 400 con el mensaje «El pago simulado solo admite TARJETA o YAPE».
2. La validación ocurre antes de bloquear el pedido y descontar el stock. Un pago con método inexistente deja el pedido PENDIENTE, no guarda pago y no modifica el stock.
3. La edición `PUT /api/pagos/{id}` valida el método con la misma regla y solo acepta el estado `APROBADO`, que es el único estado que crea el sistema. Antes se podía guardar cualquier texto.
4. En `requests.http`, la solicitud 43 (pago duplicado) usaba `EFECTIVO`. Se cambió a `YAPE` para que siga comprobando el 409 por pago duplicado y no se rechace antes por el método.
5. No cambian el JSON de `/api/pagos`, el checkout, el frontend ni Supabase. El endpoint antiguo no exige la referencia `DEMO-APROBADO` para mantener la compatibilidad con las solicitudes existentes. La eliminación de pagos no cambia.

#### Pruebas realizadas

Se agregaron 5 ejecuciones a `PagoIntegridadIntegrationTest`.

| Caso | Resultado |
| --- | --- |
| Pago con `METODO_INEXISTENTE` | 400; pedido PENDIENTE, stock sin cambios y ningún pago guardado. |
| Pago con ` tarjeta ` en minúsculas y con espacios | 201; se guarda como `TARJETA` y se descuenta el stock. |
| Edición con método `BITCOIN` | 400; el pago conserva método y estado. |
| Edición con estado `LO_QUE_SEA` | 400; el pago conserva método y estado. |
| Edición con `tarjeta` y `aprobado` | 200; se guardan normalizados como `TARJETA` y `APROBADO`. |

Antes de corregir el código fallaron los 3 casos de rechazo: el pago se aceptó con 201 y la edición con 200. Después del cambio pasaron todos.

Suite completa del backend: **87 pruebas, 0 fallos, 0 errores y 0 omitidas**, con H2 y la importación de `.env` deshabilitada.

#### Archivos involucrados

- [PagoService.java](src/main/java/com/fastfood/fast_food_backend/services/PagoService.java).
- [PagoIntegridadIntegrationTest.java](src/test/java/com/fastfood/fast_food_backend/security/PagoIntegridadIntegrationTest.java).
- [Solicitudes de API](requests.http).

### Punto 15. Creación de productos, categorías e inventarios con el ID de otro registro

**Fecha de corrección:** 1 de noviembre de 2026.
**Estado:** resuelto y verificado.

**Problema:** `POST /api/productos`, `POST /api/categorias` y `POST /api/inventarios` guardaban la entidad recibida con el `id` que traía el JSON. Si ese `id` ya existía, la API no creaba un registro nuevo sino que reemplazaba el existente. Por ejemplo, un producto «Inca Kola» con ID 3 pasaba a llamarse «Hamburguesa doble», y los pedidos antiguos mostraban el producto cambiado. Los dos primeros endpoints son del administrador y el de inventarios también lo puede usar el empleado. Angular no envía `id` al crear, así que el fallo aparecía al usar Postman, `requests.http` o una petición manual.

#### Qué se hizo para corregirlo

1. `ProductoService.registrar` y `CategoriaService.registrar` descartan el `id` recibido antes de guardar. La base de datos genera siempre un identificador nuevo.
2. `InventarioService.registrar` descarta el `id` y las fechas de stock bajo y agotado recibidas. El servidor calcula esas fechas con el stock actual del producto, como ya lo hacía.
3. La edición mediante `PUT` no cambia: sigue usando el `id` de la URL y conserva el identificador del registro.
4. No cambian el JSON, los permisos, las validaciones, el frontend ni Supabase.

#### Pruebas realizadas

Se agregó `RegistroCatalogoIntegrationTest`, con 3 casos ejecutados mediante MockMvc.

| Caso | Resultado |
| --- | --- |
| Crear un producto con el ID de otro producto | 201 con un ID nuevo; el producto original conserva nombre, precio y stock. |
| Crear una categoría con el ID de otra categoría | 201 con un ID nuevo; la categoría original conserva nombre, descripción y estado. |
| El empleado crea un inventario con el ID de otra configuración y fechas de alerta inventadas | 201 con un ID nuevo; la configuración original conserva su producto y no se guardan las fechas enviadas. |

Antes de corregir el código fallaron los 3 casos: la API devolvió el mismo ID y sobrescribió el registro. Después del cambio pasaron.

Suite completa del backend: **90 pruebas, 0 fallos, 0 errores y 0 omitidas**, con H2 y la importación de `.env` deshabilitada.

#### Archivos involucrados

- [ProductoService.java](src/main/java/com/fastfood/fast_food_backend/services/ProductoService.java).
- [CategoriaService.java](src/main/java/com/fastfood/fast_food_backend/services/CategoriaService.java).
- [InventarioService.java](src/main/java/com/fastfood/fast_food_backend/services/InventarioService.java).
- [RegistroCatalogoIntegrationTest.java](src/test/java/com/fastfood/fast_food_backend/security/RegistroCatalogoIntegrationTest.java).

### Puntos 4, 10 y 12. Checkout, regreso del detalle y mensaje del login

**Fecha de corrección:** 1 de noviembre de 2026.
**Estado:** resueltos y verificados con pruebas de Angular.

#### Qué se hizo para corregirlos

1. **Punto 4.** El campo de dirección tenía el atributo `required` en el HTML. Al elegir domicilio, Angular agregaba ese validador al formulario, y al volver a recojo el campo desaparecía sin que se recalculara la validez, por lo que el formulario seguía inválido. Se quitó el atributo, que se reemplazó por `aria-required="true"`. Ahora `checkout.ts` escucha el cambio del tipo de entrega: con domicilio la dirección es obligatoria, con recojo deja de serlo, se recalcula la validez y se borra el aviso anterior. Se conserva la comprobación de que la dirección no esté formada solo por espacios.
2. **Punto 10.** `detalle-pedido` identifica si se abrió desde `/gestion/pedidos/:id` o desde `/mis-pedidos/:id`. Desde gestión, el enlace muestra «← Pedidos» y lleva a `/gestion/pedidos`; desde la cuenta del cliente conserva «← Mis pedidos».
3. **Punto 12.** En la pantalla de inicio de sesión, una respuesta 401 muestra «El correo o la contraseña son incorrectos.». En las demás pantallas, el 401 sigue mostrando el aviso de sesión inválida o vencida.
4. No cambian el backend, Supabase, las rutas, los permisos ni el cuerpo enviado a `/api/checkout`.

#### Pruebas realizadas

| Caso | Resultado |
| --- | --- |
| Elegir domicilio, intentar confirmar sin dirección y volver a recojo | El aviso se borra y la confirmación envía `/api/checkout` con `RECOJO`. |
| Abrir el detalle desde `/gestion/pedidos/5` | El enlace lleva a `/gestion/pedidos`. |
| Abrir el detalle desde `/mis-pedidos/5` | El enlace lleva a `/mis-pedidos`. |
| Login con respuesta 401 | Se muestra el aviso de correo o contraseña incorrectos. |

Antes de corregir el código fallaron los casos de checkout, detalle desde gestión y login. Después del cambio pasaron. Frontend: **43 pruebas correctas en 36 archivos** y compilación de producción correcta. No se repitió el recorrido manual en el navegador.

#### Archivos involucrados

- [checkout.ts](front-end/src/app/components/checkout/checkout.ts), [checkout.html](front-end/src/app/components/checkout/checkout.html) y [checkout.spec.ts](front-end/src/app/components/checkout/checkout.spec.ts).
- [detalle-pedido.ts](front-end/src/app/components/detalle-pedido/detalle-pedido.ts), [detalle-pedido.html](front-end/src/app/components/detalle-pedido/detalle-pedido.html) y [detalle-pedido.spec.ts](front-end/src/app/components/detalle-pedido/detalle-pedido.spec.ts).
- [inicio-sesion.ts](front-end/src/app/components/inicio-sesion/inicio-sesion.ts) e [inicio-sesion.spec.ts](front-end/src/app/components/inicio-sesion/inicio-sesion.spec.ts).

### Puntos 13 y 16. Errores de solicitud respondidos como 500

**Fecha de corrección:** 1 de noviembre de 2026.
**Estado:** resueltos y verificados.

**Problema:** `ManejadorGlobalExcepciones` no tenía reglas para varios errores que corresponden a quien envía la solicitud. Esos errores llegaban al manejador general, que respondía 500 «Ha ocurrido un error interno en el servidor» y registraba un error grave en el log. Ocurría al subir una imagen sin el campo `archivo` (punto 13), al omitir parámetros obligatorios como `nombre` en `/api/productos/buscar` o `maximo` en `/api/productos/rango-precios`, al usar un método HTTP que la ruta no admite y al enviar un tipo de contenido incompatible (punto 16).

#### Qué se hizo para corregirlo

Se agregaron cuatro manejadores con el mismo formato `ApiError` que utilizan los demás:

| Situación | Respuesta |
| --- | --- |
| Falta un parámetro obligatorio | 400 «Falta el parámetro obligatorio 'nombre'» |
| Falta el archivo en una solicitud multipart | 400 «Falta el archivo de imagen en el campo 'archivo'» |
| Método HTTP no admitido por la ruta | 405 «El método PATCH no está permitido en esta ruta» |
| Tipo de contenido incompatible | 415 «El tipo de contenido enviado no es compatible con esta ruta» |

Las solicitudes válidas, los permisos, la lógica de negocio y el frontend no cambian. Los errores que no pertenecen a estos casos siguen respondiendo 500.

#### Pruebas realizadas

Se agregó `SolicitudIncompletaTest`, con 5 casos mediante MockMvc: búsqueda sin `nombre`, rango sin `maximo`, imagen sin archivo, imagen enviada como JSON y `PATCH` sobre un producto. Las rutas protegidas se probaron con un token real de administrador.

Antes de corregir el código, los 5 casos respondieron 500. Después del cambio respondieron 400, 400, 400, 415 y 405 según corresponde.

Suite completa del backend: **95 pruebas, 0 fallos, 0 errores y 0 omitidas**, con H2 y la importación de `.env` deshabilitada.

#### Archivos involucrados

- [ManejadorGlobalExcepciones.java](src/main/java/com/fastfood/fast_food_backend/exceptions/ManejadorGlobalExcepciones.java).
- [SolicitudIncompletaTest.java](src/test/java/com/fastfood/fast_food_backend/controllers/SolicitudIncompletaTest.java).

### Punto 6. Inventarios de los productos existentes

**Fecha de corrección:** 1 de noviembre de 2026.
**Estado:** resuelto en el código y verificado con H2. Se aplicará a Supabase la próxima vez que se inicie el backend con el archivo `.env`.

**Decisión del usuario:** los productos que no tienen configuración de inventario reciben un stock mínimo de **5**. Los productos creados desde la aplicación conservan su regla actual de mínimo 0, ajustable desde el panel.

#### Qué se hizo para corregirlo

1. `ProductoRepository.buscarSinInventario` obtiene los productos que no tienen una fila en `inventarios`.
2. `InventarioService.completarFaltantes` crea la configuración de esos productos con el mínimo indicado. `crearParaProductoNuevo` utiliza la misma lógica con mínimo 0, de modo que su comportamiento no cambia.
3. `InicializadorDatos` ejecuta `completarFaltantes(5)` al iniciar el backend, después de las categorías y del administrador.
4. Solo se agregan filas. No se modifican el stock, los precios, los pedidos ni las configuraciones de inventario que ya existen. En los arranques siguientes no se crean filas porque todos los productos ya tienen configuración.
5. Las fechas de stock bajo y agotado se registran con el momento en que se detecta el estado: stock menor o igual que 5 para stock bajo y stock 0 para agotado. No se inventan fechas históricas.

#### Pruebas realizadas

| Caso | Resultado |
| --- | --- |
| Productos sin inventario con stock 20, 3 y 0, y un producto ya configurado con mínimo 2 | Se crearon 3 configuraciones con mínimo 5; las fechas correspondieron a cada stock y la configuración existente conservó su mínimo y fechas. |
| Ejecutar el completado dos veces | La segunda ejecución no creó filas. |
| Arranque de la aplicación | `InicializadorDatos` solicita el completado con mínimo 5. |

Suite completa del backend: **98 pruebas, 0 fallos, 0 errores y 0 omitidas**, con H2 y la importación de `.env` deshabilitada. No se ejecutó contra Supabase.

#### Archivos involucrados

- [ProductoRepository.java](src/main/java/com/fastfood/fast_food_backend/repositories/ProductoRepository.java).
- [InventarioService.java](src/main/java/com/fastfood/fast_food_backend/services/InventarioService.java).
- [InicializadorDatos.java](src/main/java/com/fastfood/fast_food_backend/config/InicializadorDatos.java).
- [InventarioFaltanteIntegrationTest.java](src/test/java/com/fastfood/fast_food_backend/services/InventarioFaltanteIntegrationTest.java) e [InicializadorDatosTest.java](src/test/java/com/fastfood/fast_food_backend/config/InicializadorDatosTest.java).
- [README general](README.md).

El siguiente punto pendiente se elegirá cuando el usuario lo indique.
