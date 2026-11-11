import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { esReintentable, mensajeError } from '../../core/errores';
import { PedidosService } from '../../core/pedidos.service';
import { Pedido } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports: [DatePipe, RouterLink, MensajeError], selector: 'app-mis-pedidos', templateUrl: './mis-pedidos.html', styleUrl: './mis-pedidos.scss' })
export class MisPedidos implements OnInit {
  private readonly servicio = inject(PedidosService); readonly pedidos = signal<Pedido[]>([]); readonly error = signal(''); readonly cargando = signal(true); readonly reintentable = signal(false);
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.error.set(''); this.reintentable.set(false); this.cargando.set(true); this.servicio.mios().subscribe({next: p=>{this.pedidos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}}); }
}
