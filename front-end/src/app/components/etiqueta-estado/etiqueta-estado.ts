import { Component, computed, input } from '@angular/core';

type Tono = 'exito' | 'peligro' | 'alerta' | 'info' | 'admin' | 'neutro';

// Color de cada estado o rol que devuelve la API. Lo que no está aquí se muestra en gris.
const TONOS: Record<string, Tono> = {
  CONFIRMADO: 'exito', APROBADO: 'exito', ACTIVO: 'exito', ACTIVA: 'exito', DISPONIBLE: 'exito',
  PENDIENTE: 'alerta', 'STOCK BAJO': 'alerta',
  CANCELADO: 'peligro', AGOTADO: 'peligro', INACTIVO: 'peligro', INACTIVA: 'peligro',
  ADMINISTRADOR: 'admin', EMPLEADO: 'info', CLIENTE: 'neutro',
};

@Component({
  imports: [],
  selector: 'app-etiqueta-estado',
  template: '<span class="etiqueta" [class]="tono()">{{ texto() }}</span>',
  styleUrl: './etiqueta-estado.scss',
})
export class EtiquetaEstado {
  readonly valor = input.required<string>();
  private readonly clave = computed(() => (this.valor() ?? '').trim().toUpperCase());
  readonly tono = computed(() => `etiqueta ${TONOS[this.clave()] ?? 'neutro'}`);
  // "STOCK BAJO" se muestra como "Stock bajo".
  readonly texto = computed(() => this.clave().charAt(0) + this.clave().slice(1).toLowerCase());
}
