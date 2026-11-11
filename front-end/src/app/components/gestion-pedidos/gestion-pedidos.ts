import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { PedidosService } from '../../core/pedidos.service';
import { Pedido } from '../../models/product';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[IndicadorCarga, DatePipe, RouterLink,MensajeError],selector:'app-gestion-pedidos',templateUrl:'./gestion-pedidos.html',styleUrl:'./gestion-pedidos.scss' })
export class GestionPedidos implements OnInit {
 private readonly servicio=inject(PedidosService); readonly pedidos=signal<Pedido[]>([]); readonly error=signal(''); readonly cargando=signal(true);
 ngOnInit():void{this.servicio.todos().subscribe({next:p=>{this.pedidos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.cargando.set(false)}})}
}
