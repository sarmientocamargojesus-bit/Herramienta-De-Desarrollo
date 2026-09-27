# Referencia académica: semanas 8, 9 y 10

Este documento conserva de forma estructurada los conceptos, ejemplos,
actividades y productos esperados de los materiales proporcionados para poder
aplicarlos posteriormente al proyecto **Fast Food - Sistema Web de Gestión de
Pedidos y Ventas**.

## Fuentes revisadas

- `SEMANA 8-DESARROLLO WEB INTEGRADO-UTP.pdf` (29 páginas).
- `S9_s1 Front end frameworks & JSP.pdf` (31 páginas).
- `S10_s1 Framework Java Server Pages JSP.pdf` (16 páginas).

> Nota terminológica: los títulos de las semanas 9 y 10 mencionan JSP, pero el
> contenido desarrollado se concentra principalmente en **JavaServer Faces
> (JSF)**, Managed Beans, JavaBeans, navegación y ciclo de vida de JSF. JSP y
> JSF no son lo mismo, por lo que antes de implementar el front-end se deberá
> confirmar cuál tecnología exige exactamente el docente.

---

# Semana 8: Seguridad con Spring Security

## Tema y logro de la sesión

La semana estudia la configuración de seguridad básica de una API REST creada
con Spring Boot. El objetivo es controlar:

- Autenticación.
- Autorización.
- Roles.
- Permisos.
- Acceso a endpoints del back-end.

Los conocimientos previos requeridos son Spring Boot, API REST, métodos HTTP,
controladores, JPA, Hibernate, CRUD, JPQL, transacciones y conexión con bases
de datos relacionales.

## Contexto planteado

Una API que gestiona usuarios, productos y pedidos no debe permitir que una
persona ejecute operaciones sensibles solamente por conocer una URL. Spring
Security permite definir reglas diferenciadas para roles como:

- `CLIENTE` o `USER`.
- `VENDEDOR` o `SELLER`.
- `ADMIN`.

## Spring Security

Spring Security es el framework de seguridad integrado con Spring Boot para
proteger aplicaciones web y servicios REST. Permite determinar:

1. Quién intenta ingresar.
2. Si sus credenciales son válidas.
3. Qué recursos puede consultar.
4. Qué operaciones puede ejecutar.
5. Cuándo una solicitud debe ser bloqueada.

## Autenticación y autorización

- **Autenticación:** verifica la identidad del usuario.
- **Autorización:** determina qué puede hacer un usuario ya autenticado.

Un usuario puede iniciar sesión correctamente y aun así no tener autorización
para una operación administrativa.

Ejemplo:

```text
Usuario autenticado con rol CLIENTE
    -> intenta acceder a /api/admin/productos
    -> no tiene el rol requerido
    -> acceso denegado
```

## Cadena de filtros

Cada solicitud pasa por la cadena de filtros de Spring Security antes de llegar
al Controller. Los filtros pueden verificar:

- Sesión o credenciales.
- Identidad del usuario.
- Roles y permisos.
- Reglas aplicadas a la ruta y al método HTTP.

Si una regla no se cumple, el Controller no se ejecuta.

Flujo conceptual:

```text
Cliente HTTP
    -> SecurityFilterChain
    -> autenticación
    -> autorización
    -> Controller
    -> Service
    -> Repository
```

## Usuarios, credenciales y PasswordEncoder

Las contraseñas no deben almacenarse como texto visible. El material propone
utilizar `PasswordEncoder` para:

1. Codificar la contraseña antes de almacenarla.
2. Guardar solamente el valor codificado.
3. Comparar la contraseña recibida durante el acceso con su versión codificada.

## Roles y permisos

Los roles agrupan responsabilidades; los permisos representan acciones
concretas. Las reglas deben aplicar el principio de mínimo privilegio: cada
usuario recibe solamente los accesos que necesita.

Ejemplo para Fast Food:

| Rol | Responsabilidad posible |
|---|---|
| `CLIENTE` | Consultar productos y registrar sus pedidos |
| `VENDEDOR` | Registrar y administrar pedidos |
| `ADMIN` | Administrar productos, usuarios y catálogos |

## Protección de endpoints

Las rutas pueden clasificarse en:

- **Públicas:** no requieren autenticación.
- **Autenticadas:** requieren un usuario válido.
- **Restringidas:** exigen además un rol específico.

Ejemplo presentado en el material:

| Método y endpoint | Regla sugerida |
|---|---|
| `GET /api/productos` | Público |
| `POST /api/pedidos` | Usuario autenticado |
| `DELETE /api/productos/{id}` | Solo `ADMIN` |

En versiones actuales de Spring Boot, las reglas se declaran normalmente en
una configuración que expone un `SecurityFilterChain`.

## Respuestas HTTP de seguridad

- `401 Unauthorized`: no se proporcionó una autenticación válida.
- `403 Forbidden`: el usuario está autenticado, pero no tiene autorización.
- `200 OK`, `201 Created` u otro estado de éxito: la identidad y el permiso son
  correctos y la operación se ejecutó.

## Integración con la aplicación y la base de datos

La seguridad puede integrarse con:

- Usuarios almacenados en PostgreSQL.
- Roles asociados a usuarios o perfiles.
- Controladores REST.
- Reglas para que un usuario consulte solamente sus propios recursos.
- Operaciones administrativas restringidas a `ADMIN` o `VENDEDOR`.

## Buenas prácticas

- No entregar rol `ADMIN` a todos los usuarios.
- Separar lectura, registro, edición y administración.
- Proteger operaciones críticas.
- Mantener reglas claras y mínimas.
- Documentar los accesos permitidos por rol.
- Probar usuarios sin credenciales, sin permisos y autorizados.

## Caso práctico de la clase

Para una API de pedidos:

- `GET /api/productos`: acceso público.
- `POST /api/pedidos`: solamente usuarios autenticados.
- `DELETE /api/productos/{id}`: solamente administradores.

Se deben verificar los estados `401`, `403` y los estados exitosos
correspondientes.

## Práctica individual

**Propósito:** diseñar reglas de seguridad para una API REST.

Indicaciones:

1. Proponer una API sencilla con cinco endpoints.
2. Indicar el método HTTP de cada endpoint.
3. Clasificar las rutas como públicas, autenticadas o restringidas.
4. Asignar roles como `USER`, `SELLER` o `ADMIN`.
5. Definir la respuesta para:
   - Usuario sin sesión.
   - Usuario autenticado sin permiso.
   - Usuario autorizado.

**Producto esperado:** tabla individual con endpoint, método HTTP, nivel de
acceso, rol requerido y respuesta esperada.

## Práctica grupal

**Propósito:** construir una propuesta de seguridad para un módulo back-end.

Indicaciones:

1. Formar equipos de tres a cuatro estudiantes.
2. Seleccionar ventas, matrícula, reservas o inventario.
3. Definir roles y responsabilidades.
4. Organizar endpoints por nivel de acceso.
5. Proponer reglas de Spring Security.
6. Sustentar por qué cada rol puede o no ejecutar cada operación.

**Producto esperado:** diagrama de seguridad y propuesta de configuración para
proteger endpoints según roles.

## Aplicación futura al proyecto

Antes de implementar esta semana se deberá diseñar una tabla de acceso. Una
propuesta inicial para Fast Food sería:

| Endpoint | Acceso preliminar |
|---|---|
| `GET /api/productos/**` | Público |
| `POST /api/clientes` | Público o registro controlado |
| `POST /api/pedidos/procesar` | Usuario autenticado |
| `PUT /api/pedidos/**` | `VENDEDOR` o `ADMIN` |
| `POST`, `PUT`, `DELETE /api/productos/**` | `ADMIN` |

Esta tabla es una propuesta para análisis; no debe implementarse sin revisar la
semana 9 del sílabo y confirmar si la autenticación será temporal, Basic Auth o
posteriormente JWT.

---

# Semana 9: Frameworks de front-end

## Tema y logro de la sesión

La semana presenta alternativas de frameworks para diseñar la interfaz de un
proyecto Java. Parte de la arquitectura Java EE/J2EE y de la interacción entre
la capa cliente, la capa web, la lógica de negocio y la capa de datos.

## Arquitectura y patrones Java EE

J2EE integra tecnologías como:

- Servlets.
- JavaServer Pages.
- Enterprise JavaBeans.
- Capa de presentación.
- Capa de negocio.
- Capa de integración o persistencia.

Los diagramas muestran patrones de presentación como:

- Intercepting Filter.
- Front Controller.
- Application Controller.
- Context Controller.
- View Helper.
- Composite View.
- Service to Worker.
- Dispatcher View.

La idea central es separar responsabilidades y permitir que las capas se
comuniquen mediante interfaces definidas.

## Front-end y back-end

- El **front-end** se ejecuta principalmente en el navegador, presenta la
  interfaz y recoge las entradas del usuario.
- El **back-end** procesa la información, aplica reglas, accede a servidores y
  bases de datos y devuelve una respuesta.
- La separación ayuda a mantener ambas partes de forma independiente.

Flujo general:

```text
Usuario
    -> navegador y front-end
    -> interfaz HTTP
    -> back-end
    -> base de datos
```

## Tecnologías fundamentales del navegador

- **HTML:** estructura del contenido.
- **CSS:** presentación, fondos, bordes, sombras y estilos.
- **JavaScript:** lógica y comportamiento dinámico.
- **HTML5:** incorpora capacidades como audio, video, gráficos y
  geolocalización.

HTML y CSS son lenguajes de marcado y estilo; JavaScript sí es un lenguaje de
programación.

## Spring

Spring es un framework Java empresarial que puede utilizarse en varias capas.
Incluye módulos como:

- Spring Core.
- Spring MVC.
- Spring Security.
- Spring ORM.
- Acceso e integración de datos.
- AOP.
- Instrumentación.
- Testing.

Spring se basa en Inversión de Control e inyección de dependencias. Spring MVC
aprovecha estas características para procesar solicitudes web.

Ventajas descritas:

- Contenedor ligero.
- Soporte JDBC.
- Facilita aplicaciones Java EE.
- Configuración mediante XML o anotaciones.
- Compatibilidad y facilidad para realizar pruebas.

## Struts 2

Struts extiende la API Servlet y promueve el patrón MVC. Su punto de entrada es
el `Dispatcher Filter`. Procesa las solicitudes mediante:

1. Interceptores.
2. Acciones.
3. Resultados.

Ventajas señaladas:

- Documentación comunitaria.
- Reducción del tiempo de desarrollo.
- Configuración centralizada.
- Configuración con XML o anotaciones.

## Google Web Toolkit (GWT)

Conjunto de herramientas de Google para desarrollar y optimizar aplicaciones
del lado del cliente.

Características mencionadas:

- Desarrollo y depuración de AJAX usando Java.
- Reutilización de tareas comunes.
- Integración con APIs de Google.
- Internacionalización.
- Portabilidad entre navegadores.
- Abstracción de interfaz.
- Gestión de historial y marcadores.

## Grails

Framework basado en Groovy para la JVM, orientado a mejorar la productividad.

Características:

- Sintaxis compatible con Java.
- Compilación para la JVM.
- Documentación accesible.
- Sistema de complementos.
- Mapeo de objetos.
- Reutilización y modularización.

## JavaServer Faces (JSF)

JSF se presenta como el framework estándar de Java Enterprise para construir
interfaces web basadas en componentes.

Características:

- Basado en Java.
- Sigue el patrón MVC.
- Utiliza componentes de interfaz.
- Permite añadir una interfaz web a lógica Java existente.
- Forma parte del ecosistema Java Enterprise.
- Proporciona herramientas, librerías y perfiles flexibles.

## Práctica

La pregunta propuesta es:

> ¿Cuáles son las ventajas de JSF?

Respuesta resumida a conservar:

- Estándar del ecosistema Java Enterprise.
- Modelo de componentes para interfaces web.
- Integración con código Java del back-end.
- Soporte para MVC, herramientas y bibliotecas.
- Posibilidad de ampliar componentes mediante suites especializadas.

## Conclusiones de la semana

1. Una solución Java EE contiene varias capas que deben interactuar.
2. La capa cliente recopila información del usuario.
3. La capa web, la lógica y los datos se comunican con la capa cliente mediante
   interfaces.
4. Los frameworks facilitan la interacción entre las capas cliente y servidor.

## Aplicación futura al proyecto

El proyecto había planificado un front-end separado con React, Vite,
JavaScript y Bootstrap. El material, en cambio, presenta principalmente
frameworks Java tradicionales y JSF. Antes de programar se debe confirmar con
el docente si:

1. Es obligatorio utilizar JSF/JSP.
2. React puede utilizarse como alternativa moderna.
3. Se necesita una demostración académica pequeña con JSF además del front-end
   definitivo en React.

No se debe mezclar React y JSF en el mismo módulo sin una necesidad concreta.

---

# Semana 10: JSF, Managed Beans y ciclo de vida

## Tema y logro de la sesión

El material busca enseñar conceptos básicos para diseñar una interfaz Java y
destaca:

- JavaServer Faces.
- Managed Beans.
- Scopes.
- Expression Language.
- POJO.
- JavaBeans y EJB.
- Navegación.
- Ciclo de vida JSF.

## JavaServer Faces

JSF es presentado como el framework front-end nativo de Java EE. Su modelo se
basa en componentes y eventos del lado servidor.

Una página JSF puede enlazarse con clases Java mediante Expression Language:

```xhtml
#{bean}
#{bean.propiedad}
#{bean.metodo}
```

## Managed Bean

Un Managed Bean es una clase Java de respaldo utilizada por una página JSF.
Puede contener:

- Datos del formulario.
- Propiedades consultadas desde la vista.
- Métodos ejecutados por botones o eventos.
- Navegación.
- Coordinación con la lógica de negocio.

Los ámbitos o scopes mencionados son:

- Request.
- Session.
- Application.

Cada scope determina cuánto tiempo permanece disponible la instancia del Bean.

## POJO

POJO significa `Plain Old Java Object`. Es una clase Java sencilla que no
necesita extender una clase especial ni implementar contratos impuestos por un
framework.

Una clase de dominio como `Cliente`, con atributos y operaciones normales, es
un ejemplo de POJO. El concepto busca mantener el modelo desacoplado.

## JavaBean y EJB

### JavaBean

Clase Java sencilla que normalmente cumple estas reglas:

- Constructor público sin argumentos.
- Atributos privados.
- Métodos `get` y `set` para sus propiedades.
- Capacidad de serialización cuando sea necesaria.

Ejemplo conceptual:

```java
public class ClienteBean implements Serializable {
    private String nombre;
    private int edad;

    public ClienteBean() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
```

### Enterprise JavaBean (EJB)

Es un componente empresarial ejecutado dentro de un contenedor EJB/J2EE. El
contenedor puede proporcionar servicios como:

- Transacciones.
- Seguridad.
- Persistencia.

Un JavaBean común y un EJB no son equivalentes.

## Navegación JSF

Una acción de un componente puede devolver o señalar otra vista. El material
muestra una navegación mediante `h:commandButton`:

```xhtml
<h:form id="frmAcc">
    <h:commandButton value="Continuar" action="segunda.xhtml" />
</h:form>
```

## Ciclo de vida JSF

El diagrama de la clase presenta estas fases:

1. **Restore View:** crea o recupera el árbol de componentes de la página.
2. **Apply Request Values:** aplica a los componentes los valores recibidos en
   la solicitud.
3. **Process Validations:** ejecuta conversiones y validaciones.
4. **Update Model Values:** actualiza las propiedades de los Beans.
5. **Invoke Application:** ejecuta listeners, acciones y lógica de aplicación.
6. **Render Response:** renderiza la vista y genera el HTML enviado al
   navegador.

El árbol de componentes puede conservarse para solicitudes posteriores sobre
la misma vista.

## Ejemplo funcional presentado

El ejemplo visual relaciona:

```text
Formulario JSF en el navegador
    <-> Expression Language #{Bean}
    <-> Managed Bean con scope de request
    <-> proveedor JPA
    <-> tabla tb_usuario
```

La tabla de ejemplo contiene `nombre`, `clave` y `msj`. El Bean contiene
propiedades equivalentes y un método funcional, por ejemplo `login()`.

Esto representa una interfaz que recibe datos, invoca un Bean y accede a
entidades persistidas mediante JPA.

## Práctica

La pregunta propuesta es:

> ¿Cuál es el ciclo de vida de JSF?

La respuesta debe explicar las seis fases indicadas anteriormente, desde
`Restore View` hasta `Render Response`.

## Conclusiones de la semana

- JSF permite crear aplicaciones web dinámicas.
- Utiliza programación basada en eventos de componentes.
- Los eventos activan métodos y lógica del lado servidor.
- Se recomienda utilizar suites de componentes como PrimeFaces, IceFaces o
  RichFaces cuando el proyecto lo requiera.

## Aplicación futura al proyecto

Si se exige JSF, será necesario definir:

1. Dependencias compatibles con la versión de Spring Boot y Jakarta utilizada.
2. Ubicación de las páginas XHTML.
3. Beans y scopes.
4. Navegación entre catálogo, carrito, pedido e inicio de sesión.
5. Integración de los Beans con los Services actuales.
6. Componentes de interfaz y validaciones.

Si se mantiene React como front-end, los conceptos reutilizables serán la
separación front-end/back-end, navegación, formularios, validación y consumo de
servicios, pero no se implementarán Managed Beans ni el ciclo de vida JSF.

---

# Orden recomendado para aplicarlo después

1. **Semana 8:** diseñar la matriz de endpoints y roles antes de modificar el
   código.
2. Agregar Spring Security y comprobar `401`, `403` y accesos exitosos.
3. Confirmar con el docente la tecnología de presentación requerida en las
   semanas 9 y 10.
4. Si se autoriza React, crear un proyecto separado con Vite y consumir la API
   REST.
5. Si se exige JSF, preparar un módulo compatible y enlazar Beans con la capa
   Service.
6. Conservar pruebas y capturas como evidencia de cada semana.

# Estado actual

Este documento es solamente una referencia académica. En esta actualización
no se agregó Spring Security, React, JSP ni JSF al código del proyecto.
