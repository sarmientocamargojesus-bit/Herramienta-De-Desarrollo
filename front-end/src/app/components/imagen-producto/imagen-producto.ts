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
  // Sin foto registrada se usa la ilustración directamente. Si la foto acaba de cambiar (version > 0)
  // se consulta al backend, porque el producto recibido puede ser anterior al cambio.
  private readonly pedirFoto = computed(() => this.producto().tieneImagen !== false || this.version() > 0);
  readonly url = computed(() => !this.pedirFoto() || this.fallo() === this.clave()
    ? imagenIlustrativa(this.producto().categoria)
    : `/api/productos/${this.producto().id}/imagen?v=${this.version()}`);
  error(): void { this.fallo.set(this.clave()); this.imagenDisponible.emit(false); }
  cargada(): void { if (this.pedirFoto() && this.fallo() !== this.clave()) this.imagenDisponible.emit(true); }
}
