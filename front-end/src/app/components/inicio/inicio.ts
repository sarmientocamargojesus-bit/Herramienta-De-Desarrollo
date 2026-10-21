import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CarritoService } from '../../core/carrito.service';
import { CatalogoService } from '../../core/catalogo.service';
import { mensajeError } from '../../core/errores';
import { Producto } from '../../models/product';
import { CatalogoProductos } from '../catalogo-productos/catalogo-productos';
import { FiltroCategorias } from '../filtro-categorias/filtro-categorias';
import { IndicadorCarga } from '../indicador-carga/indicador-carga';
import { MensajeError } from '../mensaje-error/mensaje-error';
import { Portada } from '../portada/portada';
import { SobreNosotros } from '../sobre-nosotros/sobre-nosotros';

@Component({
  imports: [CatalogoProductos, FiltroCategorias, IndicadorCarga, MensajeError, Portada, SobreNosotros],
  selector: "app-inicio",
  styleUrl: "./inicio.scss",
  templateUrl: "./inicio.html",
})
export class Inicio implements OnInit {
  private readonly catalogo = inject(CatalogoService);
  private readonly carrito = inject(CarritoService);
  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<{ label: string; value: string }[]>([{ label: 'Todos', value: 'TODAS' }]);
  readonly seleccion = signal('TODAS');
  readonly visibles = computed(() => this.productos().filter(p => this.seleccion() === 'TODAS' || p.categoria === this.seleccion()));
  readonly cargando = signal(true);
  readonly error = signal('');
  readonly aviso = signal('');

  ngOnInit(): void {
    forkJoin({ productos: this.catalogo.productos(), categorias: this.catalogo.categorias() }).subscribe({
      next: ({ productos, categorias }) => {
        this.productos.set(productos);
        this.categorias.set([{ label: 'Todos', value: 'TODAS' }, ...categorias.map(c => ({ label: c.nombre, value: c.nombre }))]);
        this.cargando.set(false);
      },
      error: error => { this.error.set(mensajeError(error)); this.cargando.set(false); }
    });
  }

  agregar(producto: Producto): void {
    try {
      this.carrito.agregar(producto).subscribe({
        next: () => { this.aviso.set(`${producto.nombre} se agregó al carrito.`); this.error.set(''); },
        error: error => this.error.set(mensajeError(error))
      });
    } catch (error) { this.error.set(mensajeError(error)); }
  }
}
