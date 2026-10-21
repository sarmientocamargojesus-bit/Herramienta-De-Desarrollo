import { Component, input } from '@angular/core';
import { Product } from '../../models/product';
import { TarjetaProducto } from '../tarjeta-producto/tarjeta-producto';

@Component({
  selector: 'app-catalogo-productos',
  imports: [TarjetaProducto],
  templateUrl: './catalogo-productos.html',
  styleUrl: './catalogo-productos.scss'
})
export class CatalogoProductos { products = input.required<Product[]>(); }
