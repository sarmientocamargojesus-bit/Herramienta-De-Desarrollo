import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeError } from '../../core/errores';
import { Categoria } from '../../models/product';
import { DialogoConfirmacion } from '../dialogo-confirmacion/dialogo-confirmacion';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[IndicadorCarga, RouterLink,DialogoConfirmacion,MensajeError],selector:'app-gestion-categorias',templateUrl:'./gestion-categorias.html',styleUrl:'./gestion-categorias.scss' })
export class GestionCategorias implements OnInit {
  private readonly servicio=inject(CatalogoService); readonly categorias=signal<Categoria[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly elegida=signal<Categoria|null>(null);
  ngOnInit():void{this.cargar()}
  cargar():void{this.cargando.set(true);this.servicio.todasCategorias().subscribe({next:c=>{this.categorias.set(c);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.cargando.set(false)}})}
  eliminar():void{const c=this.elegida();if(!c)return;this.servicio.eliminarCategoria(c.id).subscribe({next:()=>{this.elegida.set(null);this.cargar()},error:e=>{this.elegida.set(null);this.error.set(mensajeError(e))}})}
}
