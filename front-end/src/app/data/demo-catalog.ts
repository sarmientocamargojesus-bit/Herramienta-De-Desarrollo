import { Product, ProductCategory } from '../models/product';

// Muestra la vista del catálogo mientras se incorpora el consumo de la API.
export const DEMO_PRODUCTS: Product[] = [
  { id: 1, nombre: 'Burger Brava', descripcion: 'Carne, cheddar y salsa de la casa.', precio: 24.9, categoria: 'HAMBURGUESAS', imagen: '/assets/demo/hero-fast-food.png', destacado: true },
  { id: 2, nombre: 'Combo Clásico', descripcion: 'Hamburguesa, papas y bebida.', precio: 29.9, categoria: 'COMBOS', imagen: '/assets/demo/combo.png' },
  { id: 3, nombre: 'Papas Fuego', descripcion: 'Papas con cheddar y toque picante.', precio: 12.9, categoria: 'PIQUEOS', imagen: '/assets/demo/papas.png' },
  { id: 4, nombre: 'Limonada Iqueña', descripcion: 'Limonada fresca con hierbabuena.', precio: 7.9, categoria: 'BEBIDAS', imagen: '/assets/demo/limonada.png' },
];

export const CATEGORIES: { label: string; value: ProductCategory | 'TODAS' }[] = [
  { label: 'Todos', value: 'TODAS' },
  { label: 'Hamburguesas', value: 'HAMBURGUESAS' },
  { label: 'Combos', value: 'COMBOS' },
  { label: 'Piqueos', value: 'PIQUEOS' },
  { label: 'Bebidas', value: 'BEBIDAS' },
];
