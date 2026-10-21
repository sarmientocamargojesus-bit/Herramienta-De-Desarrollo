import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { PedidosService } from '../../core/pedidos.service';
import { Pedido } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[RouterLink,MensajeError],selector:'app-gestion-pedidos',templateUrl:'./gestion-pedidos.html',styleUrl:'./gestion-pedidos.scss' })
export class GestionPedidos implements OnInit {
 private readonly servicio=inject(PedidosService); readonly pedidos=signal<Pedido[]>([]); readonly error=signal('');
 ngOnInit():void{this.servicio.todos().subscribe({next:p=>this.pedidos.set(p),error:e=>this.error.set(mensajeError(e))})}
}
