import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../core/carrito.service';
import { mensajeError } from '../../core/errores';
import { PedidosService } from '../../core/pedidos.service';
import { CheckoutRequest } from '../../models/product';
import { MensajeError } from '../mensaje-error/mensaje-error';
@Component({ imports: [ReactiveFormsModule, RouterLink, MensajeError], selector: 'app-checkout', templateUrl: './checkout.html', styleUrl: './checkout.scss' })
export class Checkout implements OnInit {
  private readonly fb = inject(FormBuilder); private readonly pedidos = inject(PedidosService); private readonly router = inject(Router);
  readonly carrito = inject(CarritoService); readonly error = signal(''); readonly enviando = signal(false); readonly cargando = signal(true);
  readonly formulario = this.fb.nonNullable.group({ tipoEntrega: ['RECOJO' as 'RECOJO'|'DOMICILIO', Validators.required], direccionEntrega: [''], observaciones: [''], metodoPago: ['YAPE' as 'YAPE'|'TARJETA', Validators.required] });
  ngOnInit(): void { this.carrito.cargar().subscribe({ next: () => this.cargando.set(false), error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); } }); }
  enviar(): void {
    const datos = this.formulario.getRawValue();
    if (this.formulario.invalid || (datos.tipoEntrega === 'DOMICILIO' && !datos.direccionEntrega.trim())) { this.formulario.markAllAsTouched(); this.error.set('Para domicilio, escribe una dirección de entrega.'); return; }
    if (!this.carrito.items().length) { this.error.set('El carrito está vacío.'); return; }
    this.enviando.set(true); this.error.set('');
    const solicitud: CheckoutRequest = { ...datos, direccionEntrega: datos.direccionEntrega.trim(), observaciones: datos.observaciones.trim(), referenciaPrueba: 'DEMO-APROBADO' };
    this.pedidos.confirmar(solicitud).subscribe({ next: r => { this.carrito.cargar().subscribe(); void this.router.navigate(['/pedido-confirmado', r.pedidoId]); }, error: e => { this.error.set(mensajeError(e)); this.enviando.set(false); } });
  }
}
