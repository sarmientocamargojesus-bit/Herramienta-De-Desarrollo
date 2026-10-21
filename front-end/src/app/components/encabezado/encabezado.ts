import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CarritoService } from '../../core/carrito.service';
import { SesionService } from '../../core/sesion.service';

@Component({
  selector: 'app-encabezado',
  imports: [RouterLink],
  templateUrl: './encabezado.html',
  styleUrl: './encabezado.scss'
})
export class Encabezado {
  readonly sesion = inject(SesionService);
  readonly carrito = inject(CarritoService);
  salir(): void { this.sesion.salir(); this.carrito.cargar().subscribe(); }
}
