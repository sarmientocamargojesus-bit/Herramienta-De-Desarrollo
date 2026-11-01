import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { Pago } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[DatePipe, MensajeError],selector:'app-gestion-pagos',templateUrl:'./gestion-pagos.html',styleUrl:'./gestion-pagos.scss' })
export class GestionPagos implements OnInit {
 private readonly servicio=inject(GestionService); readonly pagos=signal<Pago[]>([]); readonly error=signal('');
 ngOnInit():void{this.servicio.pagos().subscribe({next:p=>this.pagos.set(p),error:e=>this.error.set(mensajeError(e))})}
}
