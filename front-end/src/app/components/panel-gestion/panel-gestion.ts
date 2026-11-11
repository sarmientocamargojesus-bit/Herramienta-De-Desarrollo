import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CarritoService } from '../../core/carrito.service';
import { SesionService } from '../../core/sesion.service';
import { AvisoExito } from '../aviso-exito/aviso-exito';
@Component({ imports: [RouterOutlet, RouterLink, RouterLinkActive, AvisoExito], selector: 'app-panel-gestion', templateUrl: './panel-gestion.html', styleUrl: './panel-gestion.scss' })
export class PanelGestion {
  readonly sesion = inject(SesionService); private readonly carrito = inject(CarritoService); private readonly router = inject(Router);
  salir(): void { this.sesion.salir(); this.carrito.cargar().subscribe(); void this.router.navigateByUrl('/'); }
}
