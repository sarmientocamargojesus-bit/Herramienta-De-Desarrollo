import { Component, input } from '@angular/core';
import { Product } from '../../models/product';

@Component({
  selector: 'app-tarjeta-producto',
  templateUrl: './tarjeta-producto.html',
  styleUrl: './tarjeta-producto.scss'
})
export class TarjetaProducto { product = input.required<Product>(); }
