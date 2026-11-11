import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { esReintentable, mensajeError } from '../../core/errores';
import { PedidosService } from '../../core/pedidos.service';
import { Pedido } from '../../models/product';
import { EtiquetaEstado } from '../etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports: [DatePipe, DecimalPipe, RouterLink, EtiquetaEstado, MensajeError], selector: 'app-mis-pedidos', templateUrl: './mis-pedidos.html', styleUrl: './mis-pedidos.scss' })
export class MisPedidos implements OnInit {
  private readonly servicio = inject(PedidosService); readonly pedidos = signal<Pedido[]>([]); readonly error = signal(''); readonly cargando = signal(true); readonly reintentable = signal(false);
  // El pedido más reciente aparece primero.
  readonly ordenados = computed(() => [...this.pedidos()].sort((a, b) => b.fecha.localeCompare(a.fecha)));
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.error.set(''); this.reintentable.set(false); this.cargando.set(true); this.servicio.mios().subscribe({next: p=>{this.pedidos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}}); }
}
