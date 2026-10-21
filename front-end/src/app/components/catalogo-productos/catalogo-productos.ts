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
  styles: [`.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(min(100%,240px),1fr));gap:20px;margin-top:24px}.grid p{color:#756e65}`]
})
export class CatalogoProductos { products = input.required<Product[]>(); }
