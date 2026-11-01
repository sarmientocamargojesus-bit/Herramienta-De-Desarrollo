import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CarritoService } from '../../core/carrito.service';
import { mensajeError } from '../../core/errores';
import { SesionService } from '../../core/sesion.service';
import { MensajeError } from '../mensaje-error/mensaje-error';

@Component({ imports: [ReactiveFormsModule, RouterLink, MensajeError], selector: 'app-inicio-sesion', templateUrl: './inicio-sesion.html', styleUrl: './inicio-sesion.scss' })
export class InicioSesion {
  private readonly fb = inject(FormBuilder);
  private readonly sesion = inject(SesionService);
  private readonly carrito = inject(CarritoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly formulario = this.fb.nonNullable.group({ correo: ['', [Validators.required, Validators.email]], contrasena: ['', Validators.required] });
  readonly enviando = signal(false);
  readonly error = signal('');

  enviar(): void {
    if (this.formulario.invalid) { this.formulario.markAllAsTouched(); return; }
    this.enviando.set(true);
    this.error.set('');
    const { correo, contrasena } = this.formulario.getRawValue();
    this.sesion.iniciar(correo, contrasena).subscribe({
      next: respuesta => {
        if (respuesta.rol !== 'CLIENTE') { this.enviando.set(false); void this.router.navigateByUrl('/gestion'); return; }
        this.carrito.importarInvitado().subscribe({
          next: () => { this.enviando.set(false); this.irDestino(); },
          error: error => { this.enviando.set(false); this.error.set('Iniciaste sesión, pero no se pudo importar el carrito. Tus productos siguen guardados en este navegador. ' + mensajeError(error)); }
        });
      },
      // En el login, un 401 significa credenciales incorrectas, no una sesión vencida.
      error: error => {
        this.enviando.set(false);
        this.error.set(error instanceof HttpErrorResponse && error.status === 401
          ? 'El correo o la contraseña son incorrectos.' : mensajeError(error));
      }
    });
  }

  private irDestino(): void {
    const destino = this.route.snapshot.queryParamMap.get('volver');
    void this.router.navigateByUrl(destino?.startsWith('/') && !destino.startsWith('//') ? destino : '/carrito');
  }
}
