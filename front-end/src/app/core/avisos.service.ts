import { Injectable, signal } from '@angular/core';

export interface EnlaceAviso { texto: string; ruta: string; }

// Servicio compartido para avisos de éxito. Los componentes lo llaman solo cuando la API confirma
// la operación; el aviso sigue visible aunque la vista cambie de ruta.
@Injectable({ providedIn: 'root' })
export class AvisosService {
  private readonly texto = signal('');
  private readonly accion = signal<EnlaceAviso | null>(null);
  readonly mensaje = this.texto.asReadonly();
  // Enlace opcional junto al mensaje, por ejemplo "Ver carrito".
  readonly enlace = this.accion.asReadonly();
  private temporizador?: ReturnType<typeof setTimeout>;

  exito(mensaje: string, duracionMs = 4000, enlace: EnlaceAviso | null = null): void {
    clearTimeout(this.temporizador);
    this.texto.set(mensaje);
    this.accion.set(enlace);
    this.temporizador = setTimeout(() => this.cerrar(), duracionMs);
  }

  cerrar(): void {
    clearTimeout(this.temporizador);
    this.texto.set('');
    this.accion.set(null);
  }
}
