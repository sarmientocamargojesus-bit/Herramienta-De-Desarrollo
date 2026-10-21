import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { imagenIlustrativa } from '../../data/demo-catalog';
import { Producto } from '../../models/product';

@Component({
  selector: 'app-tarjeta-producto',
  imports: [RouterLink],
  templateUrl: './tarjeta-producto.html',
  styleUrl: './tarjeta-producto.scss'
})
export class TarjetaProducto {
  product = input.required<Producto>();
  agregar = output<Producto>();
  readonly imagenIlustrativa = imagenIlustrativa;
}
