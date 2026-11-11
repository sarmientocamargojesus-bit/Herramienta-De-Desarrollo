import { Injectable, signal } from '@angular/core';

// Servicio compartido para avisos de éxito. Los componentes lo llaman solo cuando la API confirma
// la operación; el aviso sigue visible aunque la vista cambie de ruta.
@Injectable({ providedIn: 'root' })
export class AvisosService {
  private readonly texto = signal('');
  readonly mensaje = this.texto.asReadonly();
  private temporizador?: ReturnType<typeof setTimeout>;

  exito(mensaje: string, duracionMs = 4000): void {
    clearTimeout(this.temporizador);
    this.texto.set(mensaje);
    this.temporizador = setTimeout(() => this.texto.set(''), duracionMs);
  }

  cerrar(): void {
    clearTimeout(this.temporizador);
    this.texto.set('');
  }
}
