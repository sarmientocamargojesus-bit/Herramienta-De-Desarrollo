-- Ejecutar una vez en el SQL Editor del proyecto Supabase antes de iniciar el backend.
-- No modifica productos existentes ni guarda datos de tarjetas o pagos.
CREATE TABLE IF NOT EXISTS public.imagenes_producto (
    producto_id BIGINT PRIMARY KEY REFERENCES public.productos(id) ON DELETE CASCADE,
    tipo_contenido VARCHAR(30) NOT NULL,
    datos BYTEA NOT NULL,
    CONSTRAINT ck_imagenes_producto_tipo
        CHECK (tipo_contenido IN ('image/png', 'image/jpeg', 'image/webp')),
    CONSTRAINT ck_imagenes_producto_tamano
        CHECK (octet_length(datos) BETWEEN 1 AND 2000000)
);

-- La API pública de Supabase no debe acceder directamente a los bytes.
-- La aplicación usa la conexión PostgreSQL del backend y protege PUT/DELETE con JWT.
ALTER TABLE public.imagenes_producto ENABLE ROW LEVEL SECURITY;
REVOKE ALL PRIVILEGES ON public.imagenes_producto FROM anon, authenticated;
