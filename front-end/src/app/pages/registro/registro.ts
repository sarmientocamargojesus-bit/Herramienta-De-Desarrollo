import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { mensajeError } from '../../shared/errores';
import { SesionService } from '../../services/sesion.service';
import { camposIguales, noSoloEspacios } from '../../shared/validadores';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';

@Component({ imports: [ReactiveFormsModule, RouterLink, MensajeError], selector: 'app-registro', templateUrl: './registro.html', styleUrl: './registro.scss' })
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly sesion = inject(SesionService);
  private readonly router = inject(Router);
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
  readonly verContrasena = signal(false);
  enviar(): void {
    if (this.formulario.invalid) { this.formulario.markAllAsTouched(); return; }
    this.enviando.set(true); this.error.set('');
    // La confirmación solo se valida en el navegador; al backend llegan los mismos datos de siempre.
    const { confirmarContrasena, ...datos } = this.formulario.getRawValue();
    this.sesion.registrar(datos).subscribe({
      // Al crear la cuenta se pasa al login con el correo ya escrito (en el estado, no en la URL).
      next: () => { this.enviando.set(false); void this.router.navigateByUrl('/iniciar-sesion', { state: { correo: datos.correo.trim().toLowerCase() } }); },
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
