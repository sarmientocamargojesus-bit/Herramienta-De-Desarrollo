import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { mensajeError } from '../../core/errores';
import { PedidosService } from '../../core/pedidos.service';
import { DetallePedido as Linea, Pedido } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports: [RouterLink, MensajeError], selector: 'app-detalle-pedido', templateUrl: './detalle-pedido.html', styleUrl: './detalle-pedido.scss' })
export class DetallePedido implements OnInit {
  private readonly ruta = inject(ActivatedRoute); private readonly servicio = inject(PedidosService);
  readonly pedido = signal<Pedido|null>(null); readonly detalles = signal<Linea[]>([]); readonly error = signal(''); readonly cargando = signal(true);
  ngOnInit(): void { const id=Number(this.ruta.snapshot.paramMap.get('id')); forkJoin({pedido:this.servicio.obtener(id),detalles:this.servicio.detalles(id)}).subscribe({next:r=>{this.pedido.set(r.pedido);this.detalles.set(r.detalles);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.cargando.set(false)}}); }
}
