import { Component, computed, input, output, signal } from '@angular/core';
import { imagenIlustrativa } from '../../data/demo-catalog';
import { Producto } from '../../models/product';

@Component({
  imports: [],
  selector: "app-imagen-producto",
  styleUrl: "./imagen-producto.scss",
  templateUrl: "./imagen-producto.html",
})
export class ImagenProducto {
  readonly producto = input.required<Producto>();
  readonly version = input(0);
  readonly imagenDisponible = output<boolean>();
  private readonly fallo = signal('');
  private readonly clave = computed(() => `${this.producto().id}:${this.version()}`);
  readonly url = computed(() => this.fallo() === this.clave()
    ? imagenIlustrativa(this.producto().categoria)
    : `/api/productos/${this.producto().id}/imagen?v=${this.version()}`);
  error(): void { this.fallo.set(this.clave()); this.imagenDisponible.emit(false); }
  cargada(): void { if (this.fallo() !== this.clave()) this.imagenDisponible.emit(true); }
}
