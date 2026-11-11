import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { esReintentable, mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { Usuario } from '../../models/product';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[IndicadorCarga, RouterLink,MensajeError],selector:'app-gestion-usuarios',templateUrl:'./gestion-usuarios.html',styleUrl:'./gestion-usuarios.scss' })
export class GestionUsuarios implements OnInit {
 private readonly servicio=inject(GestionService); readonly usuarios=signal<Usuario[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false);
 ngOnInit():void{this.cargar()}
 cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.usuarios().subscribe({next:u=>{this.usuarios.set(u);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
}
