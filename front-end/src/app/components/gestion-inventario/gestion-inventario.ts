import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { GestionService } from '../../core/gestion.service';
import { Inventario } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports:[DatePipe, RouterLink,MensajeError],selector:'app-gestion-inventario',templateUrl:'./gestion-inventario.html',styleUrl:'./gestion-inventario.scss' })
export class GestionInventario implements OnInit {
 private readonly servicio=inject(GestionService); readonly inventarios=signal<Inventario[]>([]); readonly error=signal('');
 ngOnInit():void{this.servicio.inventarios().subscribe({next:i=>this.inventarios.set(i),error:e=>this.error.set(mensajeError(e))})}
}
