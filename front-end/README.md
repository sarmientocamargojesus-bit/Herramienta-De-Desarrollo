# Fast Food — frontend Angular

Este frontend consume el backend Spring Boot del mismo repositorio. Se creó con Angular CLI y usa componentes independientes, signals, rutas con carga diferida, formularios reactivos, un interceptor HTTP con JWT y estilos SASS organizados en parciales. No incluye un catálogo de demostración: los productos y las categorías se leen de la API.

## Ejecutar

1. Inicia el backend en `http://localhost:8080` con sus variables de entorno configuradas. La base de datos se configura solo en el backend; el navegador no debe conocer las credenciales de Supabase.
2. En la carpeta `front-end`, ejecuta `npm install` y luego `npm start`.
3. Abre `http://localhost:4200`. El proxy de desarrollo envía `/api` al backend. No cambies la URL del navegador al puerto 8080 para usar Angular.

| Comando | Para qué sirve |
| --- | --- |
| `npm start` | Servidor de desarrollo con el proxy `proxy.conf.json` |
| `npm run build` | Compilación de producción en `dist/front-end` |
| `npm test -- --watch=false` | Ejecuta todas las pruebas una vez (Vitest) |
| `npx prettier --write src` | Da formato al código con las reglas de `.prettierrc` |

## Tecnologías: qué son, cómo se usan y dónde están

### Herramientas del proyecto

| Tecnología | Versión | Cómo se usa | Dónde está |
| --- | --- | --- | --- |
| Angular | ^22.2 | Framework de la aplicación de una sola página | `package.json`, `src/main.ts`, `src/app/app.config.ts` |
| Angular CLI / `@angular/build` | ^22.2.3 | Compila, sirve y prueba el proyecto | `angular.json` (`build`, `serve`, `test`) |
| TypeScript | ~6.0.2 | Lenguaje de todo el código, con tipos para los datos de la API | `tsconfig.json`, `tsconfig.app.json`, `tsconfig.spec.json` |
| RxJS | ~7.8 | Peticiones HTTP y flujos asíncronos con operadores | `src/app/services/*.service.ts`, `pages/inicio/inicio.ts` |
| SASS (SCSS) | incluido en `@angular/build` | Estilos con variables, mixins, anidación y `@each` | `src/styles.scss`, `src/styles/`, cada `.scss` de `pages/`, `components/` y `shared/` |
| Vitest + jsdom | ^5 / ^30 | Pruebas unitarias e integración en un DOM simulado | archivos `*.spec.ts` |
| Prettier | ^3.8 | Formato uniforme del código y las plantillas | `.prettierrc` |
| Proxy de desarrollo | — | Redirige `/api` a `http://127.0.0.1:8080` para evitar CORS | `proxy.conf.json` |

### Angular

| Concepto | Cómo se usa | Dónde está |
| --- | --- | --- |
| Componentes standalone | Cada componente declara sus propios `imports`; no hay `NgModule` | `src/app/pages/`, `src/app/components/` y `src/app/shared/` |
| Configuración de la aplicación | `provideRouter`, `provideHttpClient` con el interceptor y `withInMemoryScrolling` (cada página empieza arriba y los enlaces `#menu` bajan a su sección) | `src/app/app.config.ts` |
| Rutas y carga diferida | `loadComponent` carga cada vista solo cuando se visita. `/gestion` tiene rutas hijas y `**` muestra la página 404 | `src/app/app.routes.ts` |
| Protección de rutas | `rolGuard` revisa la sesión y el rol en `data.roles`. Sin sesión, lleva al login con `?volver=`; con otro rol, a `/acceso-denegado` | `src/app/guards/rol.guard.ts`, `app.routes.ts` |
| Navegación | `routerLink`, `routerLinkActive` (menú del panel), `ActivatedRoute` para leer `:id` y `?volver=`, y `Router.navigateByUrl` después de guardar | plantillas `*.html`, `pages/panel-gestion`, `inicio-sesion`, `formulario-*` |
| Signals | `signal()` guarda el estado de la pantalla (cargando, error, datos) | casi todos los componentes; `services/sesion.service.ts`, `carrito.service.ts`, `avisos.service.ts` |
| `computed()` | Valores derivados: totales, filtros, contadores y rol actual | `carrito`, `checkout`, `gestion-*`, `resumen-gestion`, `mis-pedidos`, `services/sesion.service.ts` |
| `effect()` | Recarga el carrito del navegador cuando el usuario deja de ser cliente | `services/carrito.service.ts` |
| `input()` / `output()` | Comunicación de padre a hijo en los componentes reutilizables | `tarjeta-producto`, `catalogo-productos`, `filtro-categorias`, `selector-cantidad`, `mensaje-error`, `dialogo-confirmacion`, `etiqueta-estado`, `imagen-producto` |
| Control de flujo | `@if` / `@else` para estados de carga, vacío y error; `@for` con `track` para listas y tablas | todas las plantillas `.html` de `pages/`, `components/` y `shared/` |
| Pipes | `number: '1.2-2'` para precios y `date: 'dd/MM/yyyy HH:mm'` para fechas | `carrito`, `checkout`, `mis-pedidos`, `detalle-pedido`, `gestion-*` |
| Formularios reactivos | `FormBuilder.nonNullable.group` con `Validators`; si el formulario es inválido, `markAllAsTouched()` muestra los mensajes y no se envía nada | `inicio-sesion`, `registro`, `checkout`, `cambio-rol`, `formulario-producto`, `formulario-categoria`, `formulario-inventario` |
| Validadores propios | `noSoloEspacios` rechaza textos en blanco; `camposIguales` compara la contraseña con su confirmación | `src/app/shared/validadores.ts` (usados en `registro`, `checkout`, `formulario-producto`, `formulario-categoria`) |
| Inyección de dependencias | `inject()` en componentes y servicios; `InjectionToken` `API_URL` con la ruta base `/api` | `src/app/services/api.config.ts` |
| `takeUntilDestroyed` | Cancela las suscripciones cuando se destruye el componente | `pages/inicio/inicio.ts`, `pages/checkout/checkout.ts` |
| Estilos encapsulados | Cada componente tiene su `.scss`; `::ng-deep` solo para ajustar `.pagina` dentro del panel | `*.scss` de cada componente, `pages/panel-gestion/panel-gestion.scss` |

### Comunicación con el backend (HttpClient y RxJS)

| Concepto | Cómo se usa | Dónde está |
| --- | --- | --- |
| Servicios HTTP | Los componentes no llaman a HTTP directamente: usan servicios con Observables tipados (`Producto[]`, `Pedido`, `Pago`…) | `services/catalogo.service.ts`, `pedidos.service.ts`, `gestion.service.ts`, `carrito.service.ts`, `sesion.service.ts` |
| Interceptor funcional | Agrega `Authorization: Bearer <token>` a las llamadas a `/api`. Ante un 401 cierra la sesión y, en consultas públicas, repite la petición sin token | `services/autenticacion.interceptor.ts` |
| Parámetros y archivos | `params` para la búsqueda y el control de stock; `FormData` para subir la foto del producto | `services/catalogo.service.ts` |
| `forkJoin` | Carga varias consultas a la vez y espera a que terminen todas | `inicio`, `resumen-gestion`, `detalle-pedido`, `formulario-producto` |
| `debounceTime`, `distinctUntilChanged`, `switchMap`, `catchError` | El buscador del menú espera a que el usuario deje de escribir, ignora textos repetidos y cancela la búsqueda anterior | `pages/inicio/inicio.ts` |
| `tap`, `switchMap`, `lastValueFrom` | El carrito del cliente ejecuta los cambios en orden (`enOrden`), uno después de otro | `services/carrito.service.ts` |
| Mensajes de error | `mensajeError` convierte cada error HTTP en un texto claro (sin conexión, 401, 403, 404, 502/503/504; en los demás casos muestra el mensaje que envía el backend, por ejemplo un 409 por falta de stock); `esReintentable` decide si se muestra **Reintentar** | `shared/errores.ts`, `shared/mensaje-error` |

### Sesión y almacenamiento en el navegador

| Dato | Dónde se guarda | Detalle |
| --- | --- | --- |
| Sesión (token JWT, rol, nombre, vencimiento) | `sessionStorage` | `services/sesion.service.ts`. Un temporizador (`setTimeout`) cierra la sesión cuando vence el token |
| Carrito de invitado | `localStorage` | `services/carrito.service.ts`. Al iniciar sesión como cliente se envía a `POST /api/carrito/importar` |
| Avisos de éxito | memoria (signal) | `services/avisos.service.ts`. Se ocultan solos después de unos segundos |

### Estilos con SASS

Los estilos globales están divididos en parciales dentro de `src/styles/`. `src/styles.scss` solo los reúne con `@use`.

| Archivo | Contenido |
| --- | --- |
| `_variables.scss` | Colores de marca, neutros y de estado; tipografía; medidas; mapa `$puntos` de puntos de corte |
| `_mixins.scss` | `hasta(punto)` para media queries, `mayusculas`, `enlace-acento`, `borde-lateral`, `lista-limpia`, `lista-datos`, `contenedor`, `pagina-estado`, `formulario-acceso` |
| `_base.scss` | Reinicio, tipografía, foco visible y movimiento reducido |
| `_layout.scss` | `.pagina`, `.panel`, `.grid-dos`, `.acciones`, tarjetas de resumen |
| `_formularios.scss` | `.campo`, validación (`.ng-invalid.ng-touched`), `.ayuda`, barra de filtros, "Mostrar contraseña" |
| `_botones.scss` | `.boton` y sus variantes generadas con un mapa y `@each` |
| `_tablas.scss` | `.tabla` y su versión en tarjetas para el celular (`.tabla.responsiva`) |
| `_utilidades.scss` | `.estado`, `.vacio`, `.exito`, `.contador` |

Cada componente importa las variables y los mixins con `@use 'variables' as v;` y `@use 'mixins' as m;`. Esto funciona porque `angular.json` agrega `src/styles` en `stylePreprocessorOptions.includePaths`. Ejemplos:

- `etiqueta-estado.scss` genera las clases de color con `@each` sobre un mapa.
- `gestion-inventario.scss` y `resumen-gestion.scss` colorean contadores e indicadores con `@each`.
- `encabezado.scss`, `portada.scss` y `panel-gestion.scss` usan `@include m.hasta(...)` para la versión móvil.

Las reglas de diseño adaptable (320 a 1440 px) están en `CRITERIOS_RESPONSIVOS.md`.

### Pruebas

Las pruebas usan Vitest con jsdom, `TestBed`, `HttpTestingController` (respuestas simuladas de la API) y `RouterTestingHarness` (navegación real entre rutas).

| Tipo | Dónde está |
| --- | --- |
| Por componente | `<carpeta>/<componente>/<componente>.spec.ts` dentro de `pages/`, `components/` y `shared/` |
| Servicios y utilidades | `src/app/services/*.spec.ts` (sesión, carrito, avisos, integración con la API) y `src/app/shared/*.spec.ts` (errores, validadores, texto) |
| Flujos completos | `src/app/pages/tienda.spec.ts`, `acciones-gestion.spec.ts`, `avisos-gestion.spec.ts`, `filtros-gestion.spec.ts`, `gestion-estados.spec.ts` |

## Flujo y permisos

- Sin iniciar sesión: menú, detalle de producto y carrito local. Los productos del carrito permanecen en ese navegador y guardan fecha y hora de agregado.
- Cliente: registro, inicio de sesión, importación del carrito local, checkout de prueba, confirmación e historial de pedidos. El checkout usa `/api/checkout`, que valida el stock y crea pedido y pago simulado en el backend. Nunca se capturan datos reales de tarjeta ni claves de Yape.
- Empleado: consulta pedidos e inventario y puede ajustar el stock mínimo.
- Administrador: además administra productos, categorías y roles; consulta pagos y usuarios. Cambiar el rol utiliza únicamente `/api/usuarios/{id}/rol`, sin sustituir el nombre ni otros datos.

El JWT se guarda en `sessionStorage` y se adjunta solo a llamadas del mismo origen bajo `/api/`. Las rutas comprueban el rol para orientar la navegación; el backend conserva la autoridad final sobre cada operación.

## Imágenes

El administrador puede subir, reemplazar o quitar una foto desde el formulario de producto. Se aceptan PNG, JPEG y WebP de hasta 2 MB. La foto se guarda en la tabla `imagenes_producto` mediante el backend, separada de los datos del catálogo. La imagen se consulta públicamente con `GET /api/productos/{id}/imagen`; subirla o quitarla requiere JWT de administrador. Si no hay foto, se usa la ilustración de `public/assets/demo` correspondiente a la categoría (`src/app/shared/demo-catalog.ts`). No se guardan imágenes en el navegador ni en la carpeta del frontend.

## Organización

```
front-end/
├── angular.json           configuración de Angular CLI (build, serve, test, includePaths de SASS)
├── proxy.conf.json        proxy de /api hacia el backend
├── .prettierrc            reglas de formato
├── public/assets/demo/    ilustraciones por categoría y fondo de la portada
└── src/
    ├── main.ts            arranque de la aplicación
    ├── styles.scss        reúne los parciales de SASS
    ├── styles/            variables, mixins y estilos globales
    └── app/
        ├── app.config.ts  proveedores: router, HttpClient e interceptor
        ├── app.routes.ts  rutas de la tienda y del panel de gestión
        ├── pages/         vistas con ruta propia (tienda y panel de gestión) y pruebas de flujos completos
        ├── components/    piezas de la tienda usadas por las páginas (encabezado, portada, tarjetas, filtros…)
        ├── services/      servicios HTTP, sesión, carrito, avisos, API_URL e interceptor JWT
        ├── models/        interfaces TypeScript que corresponden a la API (product.ts)
        ├── guards/        rolGuard: protección de rutas por sesión y rol
        └── shared/        componentes reutilizables (mensaje de error, carga, diálogo, etiquetas…)
                           y utilidades (validadores, errores, texto, stock, imagen de respaldo)
```

Cada componente vive en su propia carpeta con sus cuatro archivos: `.ts`, `.html`, `.scss` y `.spec.ts`.

| Carpeta | Contenido |
| --- | --- |
| `pages/` | **Tienda:** `inicio`, `detalle-producto`, `carrito`, `checkout`, `confirmacion-pedido`, `mis-pedidos`, `detalle-pedido`, `inicio-sesion`, `registro`, `acceso-denegado`, `pagina-no-encontrada` y el layout `diseno-cliente`. **Gestión:** el layout `panel-gestion`, `resumen-gestion`, `gestion-productos`, `gestion-categorias`, `gestion-inventario`, `gestion-pedidos`, `gestion-pagos`, `gestion-usuarios`, `formulario-producto`, `formulario-categoria`, `formulario-inventario` y `cambio-rol` |
| `components/` | `encabezado`, `pie-pagina`, `portada`, `sobre-nosotros`, `catalogo-productos`, `tarjeta-producto`, `filtro-categorias` |
| `services/` | `catalogo.service.ts`, `pedidos.service.ts`, `gestion.service.ts`, `carrito.service.ts`, `sesion.service.ts`, `avisos.service.ts`, `api.config.ts`, `autenticacion.interceptor.ts` |
| `models/` | `product.ts`: `Producto`, `Categoria`, `Usuario`, `Rol`, `CarritoItem`, `Pedido`, `DetallePedido`, `Pago`, `Inventario`, `CheckoutRequest`, `CheckoutResponse` |
| `guards/` | `rol.guard.ts` |
| `shared/` | Componentes: `mensaje-error`, `indicador-carga`, `dialogo-confirmacion`, `aviso-exito`, `etiqueta-estado`, `selector-cantidad`, `imagen-producto`. Utilidades: `validadores.ts`, `errores.ts`, `texto.ts`, `stock.ts`, `demo-catalog.ts` |
