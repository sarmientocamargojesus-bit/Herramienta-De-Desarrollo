import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { esReintentable, mensajeError } from '../../shared/errores';
import { PedidosService } from '../../services/pedidos.service';
import { coincide } from '../../shared/texto';
import { ESTADOS_PEDIDO, nombreEstado } from '../../shared/estados-pedido';
import { Pedido } from '../../models/product';
import { IndicadorCarga } from '../../shared/indicador-carga/indicador-carga';
import { EtiquetaEstado } from '../../shared/etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
@Component({ imports:[EtiquetaEstado, IndicadorCarga, DatePipe, DecimalPipe, RouterLink,MensajeError],selector:'app-gestion-pedidos',templateUrl:'./gestion-pedidos.html',styleUrl:'./gestion-pedidos.scss' })
export class GestionPedidos implements OnInit {
 private readonly servicio=inject(PedidosService); readonly pedidos=signal<Pedido[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false);
 readonly estados=ESTADOS_PEDIDO; readonly nombreEstado=nombreEstado;
 readonly busqueda=signal(''); readonly estado=signal('TODOS');
 // Más recientes primero; la búsqueda acepta el nombre o correo del cliente y el número de pedido.
 readonly visibles=computed(()=>[...this.pedidos()].sort((a,b)=>b.fecha.localeCompare(a.fecha)).filter(p=>(this.estado()==='TODOS'||p.estado===this.estado())&&coincide(this.busqueda().replace('#',''),p.id,p.usuario.nombre,p.usuario.correo)));
 ngOnInit():void{this.cargar()}
 cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.todos().subscribe({next:p=>{this.pedidos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
}
