import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CarritoService } from '../../services/carrito.service';
import { mensajeError } from '../../shared/errores';
import { SesionService } from '../../services/sesion.service';
import { CarritoItem } from '../../models/product';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
import { SelectorCantidad } from '../../shared/selector-cantidad/selector-cantidad';
@Component({ imports: [DatePipe, DecimalPipe, RouterLink, MensajeError, SelectorCantidad], selector: 'app-carrito', templateUrl: './carrito.html', styleUrl: './carrito.scss' })
export class Carrito implements OnInit {
  readonly servicio = inject(CarritoService); readonly sesion = inject(SesionService);
  readonly error = signal(''); readonly cargando = signal(false);
  readonly total = computed(() => this.servicio.items().reduce((s, i) => s + i.precio * i.cantidad, 0));
  readonly unidades = computed(() => this.servicio.items().reduce((s, i) => s + i.cantidad, 0));
  ngOnInit(): void { this.cargando.set(true); this.servicio.cargar().subscribe({ next: () => this.cargando.set(false), error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); } }); }
  maximo(item: CarritoItem): number { return Math.min(item.stock, 100); }
  cambiar(item: CarritoItem, cantidad: number): void { this.error.set(''); try { this.servicio.establecer(item, cantidad).subscribe({ error: e => this.error.set(mensajeError(e)) }); } catch(e) { this.error.set(mensajeError(e)); } }
  quitar(id: number): void { this.servicio.quitar(id).subscribe({ error: e => this.error.set(mensajeError(e)) }); }
}
