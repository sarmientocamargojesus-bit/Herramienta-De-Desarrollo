import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeError } from '../../core/errores';
import { Producto } from '../../models/product';
import { DialogoConfirmacion } from '../dialogo-confirmacion/dialogo-confirmacion';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[IndicadorCarga, RouterLink,DialogoConfirmacion,MensajeError],selector:'app-gestion-productos',templateUrl:'./gestion-productos.html',styleUrl:'./gestion-productos.scss' })
export class GestionProductos implements OnInit {
  private readonly servicio=inject(CatalogoService); readonly productos=signal<Producto[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly elegido=signal<Producto|null>(null);
  ngOnInit():void{this.cargar()}
  cargar():void{this.cargando.set(true);this.servicio.productos().subscribe({next:p=>{this.productos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.cargando.set(false)}})}
  eliminar():void{const p=this.elegido();if(!p)return;this.servicio.eliminarProducto(p.id).subscribe({next:()=>{this.elegido.set(null);this.cargar()},error:e=>{this.elegido.set(null);this.error.set(mensajeError(e))}})}
}
