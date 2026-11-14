import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, forkJoin, map, Observable, of, switchMap, tap } from 'rxjs';
import { AvisosService } from '../../services/avisos.service';
import { CarritoService } from '../../services/carrito.service';
import { CatalogoService } from '../../services/catalogo.service';
import { esReintentable, mensajeError } from '../../shared/errores';
import { Producto } from '../../models/product';
import { CatalogoProductos } from '../../components/catalogo-productos/catalogo-productos';
import { FiltroCategorias } from '../../components/filtro-categorias/filtro-categorias';
import { IndicadorCarga } from '../../shared/indicador-carga/indicador-carga';
import { MensajeError } from '../../shared/mensaje-error/mensaje-error';
import { Portada } from '../../components/portada/portada';
import { SobreNosotros } from '../../components/sobre-nosotros/sobre-nosotros';

@Component({
  imports: [CatalogoProductos, FiltroCategorias, IndicadorCarga, MensajeError, Portada, ReactiveFormsModule, SobreNosotros],
  selector: "app-inicio",
  styleUrl: "./inicio.scss",
  templateUrl: "./inicio.html",
})
export class Inicio implements OnInit {
  private readonly catalogo = inject(CatalogoService);
  private readonly carrito = inject(CarritoService);
  private readonly avisos = inject(AvisosService);
  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<{ label: string; value: string }[]>([{ label: 'Todos', value: 'TODAS' }]);
  readonly seleccion = signal('TODAS');
  readonly busqueda = new FormControl('', { nonNullable: true });
  readonly termino = signal('');
  readonly buscando = signal(false);
  // null: sin búsqueda activa, se muestra el menú completo.
  readonly resultados = signal<Producto[] | null>(null);
  readonly visibles = computed(() => (this.resultados() ?? this.productos())
    .filter(p => this.seleccion() === 'TODAS' || p.categoria === this.seleccion()));
  readonly cargando = signal(true);
  readonly error = signal('');
  // Solo la carga del menú ofrece Reintentar; los errores al agregar o buscar no.
  readonly reintentable = signal(false);

  constructor() {
    // Espera a que se deje de escribir y switchMap cancela la búsqueda anterior si llega un texto nuevo.
    this.busqueda.valueChanges.pipe(
      debounceTime(300),
      map(texto => texto.trim()),
      distinctUntilChanged(),
      tap(texto => { this.termino.set(texto); this.buscando.set(!!texto); }),
      switchMap(texto => texto ? this.buscar(texto) : of(null)),
      takeUntilDestroyed()
    ).subscribe(resultados => { this.resultados.set(resultados); this.buscando.set(false); });
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.error.set('');
    this.reintentable.set(false);
    this.cargando.set(true);
    forkJoin({ productos: this.catalogo.productos(), categorias: this.catalogo.categorias() }).subscribe({
      next: ({ productos, categorias }) => {
        this.productos.set(productos);
        this.categorias.set([{ label: 'Todos', value: 'TODAS' }, ...categorias.map(c => ({ label: c.nombre, value: c.nombre }))]);
        this.cargando.set(false);
      },
      error: error => { this.error.set(mensajeError(error)); this.reintentable.set(esReintentable(error)); this.cargando.set(false); }
    });
  }

  agregar(producto: Producto): void {
    this.reintentable.set(false);
    try {
      this.carrito.agregar(producto).subscribe({
        next: () => { this.avisos.exito(`${producto.nombre} se agregó al carrito.`, 4000, { texto: 'Ver carrito', ruta: '/carrito' }); this.error.set(''); },
        error: error => this.error.set(mensajeError(error))
      });
    } catch (error) { this.error.set(mensajeError(error)); }
  }

  // Un error se muestra sin cortar el flujo: la siguiente búsqueda sigue funcionando.
  private buscar(texto: string): Observable<Producto[] | null> {
    return this.catalogo.buscar(texto).pipe(
      tap(() => this.error.set('')),
      catchError(error => { this.error.set(mensajeError(error)); this.reintentable.set(false); return of(null); })
    );
  }
}
