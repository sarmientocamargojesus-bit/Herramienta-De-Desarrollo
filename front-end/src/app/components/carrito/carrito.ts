import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CarritoService } from '../../core/carrito.service';
import { mensajeError } from '../../core/errores';
import { SesionService } from '../../core/sesion.service';
import { CarritoItem } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports: [RouterLink, MensajeError], selector: 'app-carrito', templateUrl: './carrito.html', styleUrl: './carrito.scss' })
export class Carrito implements OnInit {
  readonly servicio = inject(CarritoService); readonly sesion = inject(SesionService);
  readonly error = signal(''); readonly cargando = signal(false);
  readonly total = computed(() => this.servicio.items().reduce((s, i) => s + i.precio * i.cantidad, 0));
  ngOnInit(): void { this.cargando.set(true); this.servicio.cargar().subscribe({ next: () => this.cargando.set(false), error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); } }); }
  cambiar(item: CarritoItem, valor: string): void { const cantidad = Number(valor); try { this.servicio.establecer(item, cantidad).subscribe({ error: e => this.error.set(mensajeError(e)) }); } catch(e) { this.error.set(mensajeError(e)); } }
  quitar(id: number): void { this.servicio.quitar(id).subscribe({ error: e => this.error.set(mensajeError(e)) }); }
}
