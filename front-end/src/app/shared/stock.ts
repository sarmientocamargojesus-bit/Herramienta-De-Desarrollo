// Mismo criterio que el backend: agotado con stock 0 y stock bajo al llegar al mínimo.
export function estadoStock(stock: number, stockMinimo: number): 'AGOTADO' | 'STOCK BAJO' | 'DISPONIBLE' {
  if (stock === 0) return 'AGOTADO';
  return stock <= stockMinimo ? 'STOCK BAJO' : 'DISPONIBLE';
}
