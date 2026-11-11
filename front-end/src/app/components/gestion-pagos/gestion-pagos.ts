import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { esReintentable, mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { Pago } from '../../models/product';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[IndicadorCarga, DatePipe, MensajeError],selector:'app-gestion-pagos',templateUrl:'./gestion-pagos.html',styleUrl:'./gestion-pagos.scss' })
export class GestionPagos implements OnInit {
 private readonly servicio=inject(GestionService); readonly pagos=signal<Pago[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false);
 ngOnInit():void{this.cargar()}
 cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.pagos().subscribe({next:p=>{this.pagos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
}
