import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { esReintentable, mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { Inventario } from '../../models/product';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[IndicadorCarga, DatePipe, RouterLink,MensajeError],selector:'app-gestion-inventario',templateUrl:'./gestion-inventario.html',styleUrl:'./gestion-inventario.scss' })
export class GestionInventario implements OnInit {
 private readonly servicio=inject(GestionService); readonly inventarios=signal<Inventario[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false);
 ngOnInit():void{this.cargar()}
 cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.inventarios().subscribe({next:i=>{this.inventarios.set(i);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
}
