# Fast Food — frontend Angular

Este frontend consume el backend Spring Boot del mismo repositorio. Se creó con Angular CLI y usa componentes independientes, rutas, formularios reactivos y estilos SCSS. No incluye un catálogo de demostración: los productos y las categorías se leen de la API.

## Ejecutar

1. Inicia el backend en `http://localhost:8080` con sus variables de entorno configuradas. La base de datos se configura solo en el backend; el navegador no debe conocer las credenciales de Supabase.
2. En la carpeta `front-end`, ejecuta `npm install` y luego `npm start`.
3. Abre `http://localhost:4200`. El proxy de desarrollo envía `/api` al backend. No cambies la URL del navegador al puerto 8080 para usar Angular.

Para comprobar el frontend: `npm run build` y `npm test -- --watch=false`.

## Flujo y permisos

- Sin iniciar sesión: menú, detalle de producto y carrito local. Los productos del carrito permanecen en ese navegador y guardan fecha y hora de agregado.
- Cliente: registro, inicio de sesión, importación del carrito local, checkout de prueba, confirmación e historial de pedidos. El checkout usa `/api/checkout`, que valida el stock y crea pedido y pago simulado en el backend. Nunca se capturan datos reales de tarjeta ni claves de Yape.
- Empleado: consulta pedidos e inventario y puede ajustar el stock mínimo.
- Administrador: además administra productos, categorías y roles; consulta pagos y usuarios. Cambiar el rol utiliza únicamente `/api/usuarios/{id}/rol`, sin sustituir el nombre ni otros datos.

El JWT se guarda en `sessionStorage` y se adjunta solo a llamadas del mismo origen bajo `/api/`. Las rutas comprueban el rol para orientar la navegación; el backend conserva la autoridad final sobre cada operación.

## Imágenes

Las imágenes de `public/assets/demo` son ilustrativas y reutilizables por categoría. El backend actual no tiene un campo ni un endpoint para guardar fotos de productos, por lo que la interfaz de administración aún no permite subirlas. No se simula una carga que no pueda persistirse.

## Organización

- `src/app/components`: vistas y piezas de interfaz en español. Cada componente generado tiene su propio `.scss`.
- `src/app/core`: servicios HTTP, sesión, carrito, protección de rutas y tratamiento de errores.
- `src/app/models`: contratos de datos que corresponden a la API.
- `src/styles.scss` y `src/styles/_variables.scss`: estilos generales y variables Sass.
