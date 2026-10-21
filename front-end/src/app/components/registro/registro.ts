import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { SesionService } from '../../core/sesion.service';
import { MensajeError } from '../mensaje-error/mensaje-error';

@Component({ imports: [ReactiveFormsModule, RouterLink, MensajeError], selector: 'app-registro', templateUrl: './registro.html', styleUrl: './registro.scss' })
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly sesion = inject(SesionService);
  readonly formulario = this.fb.nonNullable.group({ nombre: ['', [Validators.required, Validators.maxLength(100)]], correo: ['', [Validators.required, Validators.email]], telefono: ['', Validators.required], direccion: [''], contrasena: ['', [Validators.required, Validators.minLength(6)]] });
  readonly enviando = signal(false);
  readonly error = signal('');
  readonly listo = signal(false);
  enviar(): void {
    if (this.formulario.invalid) { this.formulario.markAllAsTouched(); return; }
    this.enviando.set(true); this.error.set('');
    this.sesion.registrar(this.formulario.getRawValue()).subscribe({ next: () => { this.listo.set(true); this.enviando.set(false); }, error: error => { this.error.set(mensajeError(error)); this.enviando.set(false); } });
  }
}
