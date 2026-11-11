import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { esReintentable, mensajeError } from '../../core/errores';
import { estadoStock } from '../../core/stock';
import { coincide } from '../../core/texto';
import { GestionService } from '../../core/gestion.service';
import { Inventario } from '../../models/product';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { EtiquetaEstado } from '../etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[EtiquetaEstado, IndicadorCarga, DatePipe, RouterLink,MensajeError],selector:'app-gestion-inventario',templateUrl:'./gestion-inventario.html',styleUrl:'./gestion-inventario.scss' })
export class GestionInventario implements OnInit {
 private readonly servicio=inject(GestionService); readonly inventarios=signal<Inventario[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false);
 readonly busqueda=signal(''); readonly soloPorReponer=signal(false);
 readonly agotados=computed(()=>this.inventarios().filter(i=>this.estadoStock(i)==='AGOTADO').length);
 readonly stockBajo=computed(()=>this.inventarios().filter(i=>this.estadoStock(i)==='STOCK BAJO').length);
 readonly disponibles=computed(()=>this.inventarios().length-this.agotados()-this.stockBajo());
 // Lo más urgente primero: agotados, luego stock bajo, y dentro de cada grupo el de menor stock.
 readonly visibles=computed(()=>this.inventarios()
   .filter(i=>(!this.soloPorReponer()||this.estadoStock(i)!=='DISPONIBLE')&&coincide(this.busqueda(),i.producto.nombre))
   .sort((a,b)=>this.prioridad(a)-this.prioridad(b)||a.producto.stock-b.producto.stock));
 ngOnInit():void{this.cargar()}
 estadoStock(i:Inventario):string{return estadoStock(i.producto.stock,i.stockMinimo)}
 private prioridad(i:Inventario):number{return {AGOTADO:0,'STOCK BAJO':1,DISPONIBLE:2}[this.estadoStock(i)]??2}
 cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.inventarios().subscribe({next:i=>{this.inventarios.set(i);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
}
