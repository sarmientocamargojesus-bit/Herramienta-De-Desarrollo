import { Component, input } from '@angular/core';
import { Product } from '../../models/product';
import { TarjetaProducto } from '../tarjeta-producto/tarjeta-producto';

@Component({
  selector: 'app-catalogo-productos',
  imports: [TarjetaProducto],
  template: `
    <div class="grid">
      @for (product of products(); track product.id) { <app-tarjeta-producto [product]="product" /> }
      @empty { <p>No hay productos de muestra en esta categoría.</p> }
    </div>
  `,
  styles: [`.grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:20px;margin-top:24px}.grid p{color:#756e65}@media(max-width:1050px){.grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:610px){.grid{grid-template-columns:1fr}}`]
})
export class CatalogoProductos { products = input.required<Product[]>(); }
