import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { SesionService } from '../../core/sesion.service';
import { camposIguales, noSoloEspacios } from '../../core/validadores';
import { MensajeError } from '../mensaje-error/mensaje-error';

@Component({ imports: [ReactiveFormsModule, RouterLink, MensajeError], selector: 'app-registro', templateUrl: './registro.html', styleUrl: './registro.scss' })
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly sesion = inject(SesionService);
  readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100), noSoloEspacios]],
    correo: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.required, noSoloEspacios]],
    direccion: [''],
    contrasena: ['', [Validators.required, Validators.minLength(6)]],
    confirmarContrasena: ['', Validators.required]
  }, { validators: camposIguales('contrasena', 'confirmarContrasena') });
  readonly enviando = signal(false);
  readonly error = signal('');
  readonly listo = signal(false);
  enviar(): void {
    if (this.formulario.invalid) { this.formulario.markAllAsTouched(); return; }
    this.enviando.set(true); this.error.set('');
    // La confirmación solo se valida en el navegador; al backend llegan los mismos datos de siempre.
    const { confirmarContrasena, ...datos } = this.formulario.getRawValue();
    this.sesion.registrar(datos).subscribe({
      next: () => { this.listo.set(true); this.enviando.set(false); },
      error: error => {
        this.enviando.set(false);
        // 409 en el registro significa que el correo ya existe: el aviso se muestra junto a ese campo.
        if (error instanceof HttpErrorResponse && error.status === 409) {
          this.formulario.controls.correo.setErrors({ registrado: true });
          this.formulario.controls.correo.markAsTouched();
        } else {
          this.error.set(mensajeError(error));
        }
      }
    });
  }
}
