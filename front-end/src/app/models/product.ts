export type ProductCategory = 'HAMBURGUESAS' | 'COMBOS' | 'PIQUEOS' | 'BEBIDAS';

export interface Product {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: ProductCategory;
  imagen: string;
  destacado?: boolean;
}
