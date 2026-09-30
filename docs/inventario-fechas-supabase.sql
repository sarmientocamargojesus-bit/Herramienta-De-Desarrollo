-- Migración aditiva para Supabase. No altera el stock de productos.
-- Ejecutar una sola vez en SQL Editor antes de usar las nuevas fechas.
BEGIN;

ALTER TABLE public.inventarios
    ADD COLUMN IF NOT EXISTS fecha_stock_bajo timestamp without time zone,
    ADD COLUMN IF NOT EXISTS fecha_agotado timestamp without time zone;

COMMIT;

-- Consulta de solo lectura: productos que aún necesitan un mínimo individual.
-- No se inventan fechas de agotamiento o stock bajo anteriores a esta migración.
SELECT p.id, p.nombre, p.stock
FROM public.productos p
LEFT JOIN public.inventarios i ON i.producto_id = p.id
WHERE i.id IS NULL
ORDER BY p.id;
