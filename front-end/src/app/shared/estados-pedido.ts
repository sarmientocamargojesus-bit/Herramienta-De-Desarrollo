// Estados de un pedido tal como los devuelve el backend.
export const ESTADOS_PEDIDO = ['PENDIENTE', 'CONFIRMADO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO'];

// Pedidos con el pago aprobado: cuentan como venta en el resumen.
export const ESTADOS_PAGADOS = ['CONFIRMADO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO'];

// El personal avanza el pedido paso a paso; el backend rechaza cualquier otro salto.
export const SIGUIENTE_ESTADO: Record<string, { estado: string; accion: string }> = {
  CONFIRMADO: { estado: 'EN_PREPARACION', accion: 'Pasar a preparación' },
  EN_PREPARACION: { estado: 'LISTO', accion: 'Marcar como listo' },
  LISTO: { estado: 'ENTREGADO', accion: 'Marcar como entregado' },
};

const NOMBRES: Record<string, string> = { EN_PREPARACION: 'En preparación' };

// "EN_PREPARACION" se muestra como "En preparación"; el resto, con mayúscula inicial.
export function nombreEstado(valor: string): string {
  const clave = (valor ?? '').trim().toUpperCase();
  if (NOMBRES[clave]) return NOMBRES[clave];
  const texto = clave.replace(/_/g, ' ');
  return texto.charAt(0) + texto.slice(1).toLowerCase();
}
