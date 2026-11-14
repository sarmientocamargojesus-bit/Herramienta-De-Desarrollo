import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AvisosService } from '../../services/avisos.service';
import { esReintentable, mensajeError } from '../../shared/errores';
import { GestionService } from '../../services/gestion.service';
import { Pago } from '../../models/product';
import { DialogoConfirmacion } from '../../shared/dialogo-confirmacion/dialogo-confirmacion';
import { IndicadorCarga } from '../../shared/indicador-carga/indicador-carga';
import { EtiquetaEstado } from '../../shared/etiqueta-estado/etiqueta-estado';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
@Component({ imports:[EtiquetaEstado, IndicadorCarga, DatePipe, DecimalPipe, RouterLink, DialogoConfirmacion, MensajeError],selector:'app-gestion-pagos',templateUrl:'./gestion-pagos.html',styleUrl:'./gestion-pagos.scss' })
export class GestionPagos implements OnInit {
 private readonly servicio=inject(GestionService); private readonly avisos=inject(AvisosService); readonly pagos=signal<Pago[]>([]); readonly error=signal(''); readonly cargando=signal(true); readonly reintentable=signal(false);
 readonly elegido=signal<Pago|null>(null); readonly anulando=signal(false);
 readonly ordenados=computed(()=>[...this.pagos()].sort((a,b)=>b.fecha.localeCompare(a.fecha)));
 readonly mensajeAnulacion=computed(()=>{const p=this.elegido();return p?`Se anulará el pago #${p.id} de S/ ${p.monto.toFixed(2)}. El stock de sus productos se devolverá y el pedido #${p.pedido.id} quedará CANCELADO. Esta acción no se puede deshacer.`:''});
 ngOnInit():void{this.cargar()}
 cargar():void{this.error.set('');this.reintentable.set(false);this.cargando.set(true);this.servicio.pagos().subscribe({next:p=>{this.pagos.set(p);this.cargando.set(false)},error:e=>{this.error.set(mensajeError(e));this.reintentable.set(esReintentable(e));this.cargando.set(false)}})}
 anular():void{const p=this.elegido();if(!p||this.anulando())return;this.anulando.set(true);this.servicio.anularPago(p.id).subscribe({next:()=>{this.elegido.set(null);this.anulando.set(false);this.avisos.exito(`Pago #${p.id} anulado. El pedido #${p.pedido.id} quedó cancelado.`);this.cargar()},error:e=>{this.elegido.set(null);this.anulando.set(false);this.reintentable.set(false);this.error.set(mensajeError(e))}})}
}
