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
| 2 | Confirmación de pedidos sin productos ni pago | Pendiente. |
| 3 | Venta simultánea de la última unidad por el pago antiguo | Pendiente. |
| 4 | Bloqueo al cambiar de domicilio a recojo | Pendiente. |
| 5 | Desincronización al renombrar categorías | Pendiente. |
| 6 | Inventarios pendientes para los productos existentes | Pendiente de definir los mínimos individuales y registrarlos. |
| 7 | Pérdida de unidades por clics rápidos en el carrito | Pendiente. |
| 8 | Tokens anteriores conservan permisos retirados | Pendiente. |
| 9 | Accesos de cuenta ocultos en móvil | Pendiente. |
| 10 | Regreso incorrecto del detalle administrativo de pedidos | Pendiente. |
| 11 | Métodos de pago inexistentes aceptados | Pendiente. |
| 12 | Mensaje de sesión vencida al fallar el login | Pendiente. |
| 13 | Respuesta 500 cuando falta el archivo de imagen | Pendiente. |

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

El siguiente punto pendiente es el **2: controlar la confirmación de pedidos**. Su implementación queda pendiente de la indicación del usuario.
