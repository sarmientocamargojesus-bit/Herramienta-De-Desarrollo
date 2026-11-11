import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AvisosService } from '../../core/avisos.service';
import { CatalogoService } from '../../core/catalogo.service';
import { esReintentable, mensajeError } from '../../core/errores';
import { coincide } from '../../core/texto';
import { Producto } from '../../models/product';
import { DialogoConfirmacion } from '../dialogo-confirmacion/dialogo-confirmacion';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { EtiquetaEstado } from '../etiqueta-estado/etiqueta-estado';
import { ImagenProducto } from '../imagen-producto/imagen-producto';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[DecimalPipe, EtiquetaEstado, ImagenProducto, IndicadorCarga, RouterLink,DialogoConfirmacion,MensajeError],selector:'app-gestion-productos',templateUrl:'./gestion-productos.html',styleUrl:'./gestion-productos.scss' })
export class GestionProductos implements OnInit {
  private readonly servicio=inject(CatalogoService); private readonly avisos=inject(AvisosService); readonly productos=signal<Producto[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false); readonly elegido=signal<Producto|null>(null);
  // Filtros locales sobre la lista ya cargada: no hacen nuevas peticiones a la API.
  readonly busqueda=signal(''); readonly categoria=signal('TODAS');
  readonly categoriasDisponibles=computed(()=>[...new Set(this.productos().map(p=>p.categoria))].sort());
  readonly visibles=computed(()=>this.productos().filter(p=>(this.categoria()==='TODAS'||p.categoria===this.categoria())&&coincide(this.busqueda(),p.nombre,p.descripcion)));
  ngOnInit():void{this.cargar()}
  cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.productos().subscribe({next:p=>{this.productos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
  eliminar():void{const p=this.elegido();if(!p)return;this.servicio.eliminarProducto(p.id).subscribe({next:()=>{this.elegido.set(null);this.avisos.exito(`Producto «${p.nombre}» eliminado.`);this.cargar()},error:e=>{this.elegido.set(null);this.reintentable.set(false);this.error.set(mensajeError(e))}})}
}
