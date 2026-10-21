import { Component, input, output } from '@angular/core';
import { Producto } from '../../models/product';
import { TarjetaProducto } from '../tarjeta-producto/tarjeta-producto';

@Component({
  selector: 'app-catalogo-productos',
  imports: [TarjetaProducto],
  templateUrl: './catalogo-productos.html',
  styleUrl: './catalogo-productos.scss'
})
export class CatalogoProductos {
  products = input.required<Producto[]>();
  agregar = output<Producto>();
}
