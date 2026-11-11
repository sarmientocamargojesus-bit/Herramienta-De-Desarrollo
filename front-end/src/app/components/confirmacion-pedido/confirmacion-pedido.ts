import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { PedidosService } from '../../core/pedidos.service';
import { Pedido } from '../../models/product';
import { EtiquetaEstado } from '../etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports: [DatePipe, DecimalPipe, RouterLink, EtiquetaEstado, MensajeError], selector: 'app-confirmacion-pedido', templateUrl: './confirmacion-pedido.html', styleUrl: './confirmacion-pedido.scss' })
export class ConfirmacionPedido implements OnInit {
  private readonly ruta = inject(ActivatedRoute); private readonly pedidos = inject(PedidosService);
  readonly pedido = signal<Pedido|null>(null); readonly error = signal('');
  ngOnInit(): void { this.pedidos.obtener(Number(this.ruta.snapshot.paramMap.get('id'))).subscribe({next: p=>this.pedido.set(p),error:e=>this.error.set(mensajeError(e))}); }
}
