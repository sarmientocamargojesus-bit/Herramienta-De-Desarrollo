import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../../models/product';
import { ImagenProducto } from '../../shared/imagen-producto/imagen-producto';

@Component({
  selector: 'app-tarjeta-producto',
  imports: [RouterLink, ImagenProducto],
  templateUrl: './tarjeta-producto.html',
  styleUrl: './tarjeta-producto.scss'
})
export class TarjetaProducto {
  product = input.required<Producto>();
  agregar = output<Producto>();
}
