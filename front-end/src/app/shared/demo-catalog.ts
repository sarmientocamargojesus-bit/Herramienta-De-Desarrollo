// Asocia imágenes ilustrativas mientras el backend no ofrece fotos de productos.
export function imagenIlustrativa(categoria: string): string {
  switch (categoria?.toUpperCase()) {
    case 'BEBIDAS': return '/assets/demo/limonada.png';
    case 'PIQUEOS': return '/assets/demo/papas.png';
    case 'COMBOS': return '/assets/demo/combo.png';
    default: return '/assets/demo/hero-fast-food.png';
  }
}
