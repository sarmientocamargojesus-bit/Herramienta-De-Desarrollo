import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../services/carrito.service';
import { mensajeError } from '../../shared/errores';
import { PedidosService } from '../../services/pedidos.service';
import { noSoloEspacios } from '../../shared/validadores';
import { CheckoutRequest } from '../../models/product';
import { IndicadorCarga } from '../../shared/indicador-carga/indicador-carga';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
@Component({ imports: [DecimalPipe, ReactiveFormsModule, RouterLink, MensajeError, IndicadorCarga], selector: 'app-checkout', templateUrl: './checkout.html', styleUrl: './checkout.scss' })
export class Checkout implements OnInit {
  private readonly fb = inject(FormBuilder); private readonly pedidos = inject(PedidosService); private readonly router = inject(Router);
  readonly carrito = inject(CarritoService);
  // Total con los precios del carrito; el backend vuelve a calcularlo al confirmar.
  readonly total = computed(() => this.carrito.items().reduce((s, i) => s + i.precio * i.cantidad, 0)); readonly error = signal(''); readonly enviando = signal(false); readonly cargando = signal(true);
  readonly formulario = this.fb.nonNullable.group({ tipoEntrega: ['RECOJO' as 'RECOJO'|'DOMICILIO', Validators.required], direccionEntrega: ['', Validators.maxLength(255)], observaciones: ['', Validators.maxLength(255)], metodoPago: ['YAPE' as 'YAPE'|'TARJETA', Validators.required] });
  constructor() {
    // La dirección solo es obligatoria para domicilio; al cambiar la entrega se recalcula la validez del formulario.
    this.formulario.controls.tipoEntrega.valueChanges.pipe(takeUntilDestroyed()).subscribe(tipo => {
      const direccion = this.formulario.controls.direccionEntrega;
      direccion.setValidators(tipo === 'DOMICILIO' ? [Validators.required, noSoloEspacios, Validators.maxLength(255)] : Validators.maxLength(255));
      direccion.updateValueAndValidity();
      this.error.set('');
    });
  }
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
