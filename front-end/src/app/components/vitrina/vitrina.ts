import { DecimalPipe } from '@angular/common';
import { Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../../models/product';
import { ImagenProducto } from '../../shared/imagen-producto/imagen-producto';

interface TarjetaCategoria { valor: string; nombre: string; cantidad: number; foto: Producto | null; }

// Secciones de la página de inicio que se arman con el catálogo real: categorías con foto,
// una selección de productos, cifras del menú y los combos. No consulta al backend:
// Inicio le pasa los productos de categorías activas (input) y recibe las acciones (output).
@Component({
  selector: 'app-vitrina',
  imports: [DecimalPipe, ImagenProducto, RouterLink],
  templateUrl: './vitrina.html',
  styleUrl: './vitrina.scss'
})
export class Vitrina {
  readonly productos = input<Producto[]>([]);
  readonly categorias = input<{ label: string; value: string }[]>([]);
  readonly categoriaElegida = output<string>();
  readonly agregar = output<Producto>();

  private readonly disponibles = computed(() => this.productos().filter(p => p.stock > 0));

  // Una tarjeta por categoría activa, con la foto de uno de sus productos (si alguno tiene foto propia).
  readonly tarjetas = computed<TarjetaCategoria[]>(() => this.categorias()
    .filter(c => c.value !== 'TODAS')
    .map(c => {
      const deLaCategoria = this.productos().filter(p => p.categoria === c.value);
      return {
        valor: c.value,
        nombre: this.nombre(c.label),
        cantidad: deLaCategoria.length,
        foto: deLaCategoria.find(p => p.tieneImagen) ?? deLaCategoria[0] ?? null,
      };
    }));

  // Seis productos con stock, tomando uno de cada categoría por turno para que la selección sea variada.
  readonly seleccion = computed(() => {
    const grupos = this.tarjetas().map(t => this.disponibles().filter(p => p.categoria === t.valor));
    const elegidos: Producto[] = [];
    for (let vuelta = 0; elegidos.length < 6 && grupos.some(g => g.length > vuelta); vuelta++) {
      for (const grupo of grupos) if (grupo[vuelta] && elegidos.length < 6) elegidos.push(grupo[vuelta]);
    }
    return elegidos;
  });

  readonly cifras = computed(() => [
    { valor: this.productos().length, texto: 'productos en el menú' },
    { valor: this.tarjetas().length, texto: this.tarjetas().length === 1 ? 'categoría' : 'categorías' },
    { valor: this.disponibles().length, texto: 'disponibles para pedir hoy' },
  ]);

  // Los tres combos con stock más económicos, de menor a mayor precio.
  readonly combos = computed(() => this.disponibles()
    .filter(p => p.categoria === 'COMBOS')
    .sort((a, b) => a.precio - b.precio)
    .slice(0, 3));

  // "HAMBURGUESAS" se muestra como "Hamburguesas".
  nombre(texto: string): string {
    return texto.charAt(0) + texto.slice(1).toLowerCase();
  }
}
