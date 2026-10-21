import { Component, computed, signal } from '@angular/core';
import { SobreNosotros } from './components/sobre-nosotros/sobre-nosotros';
import { FiltroCategorias } from './components/filtro-categorias/filtro-categorias';
import { Portada } from './components/portada/portada';
import { CatalogoProductos } from './components/catalogo-productos/catalogo-productos';
import { PiePagina } from './components/pie-pagina/pie-pagina';
import { Encabezado } from './components/encabezado/encabezado';
import { CATEGORIES, DEMO_PRODUCTS } from './data/demo-catalog';
import { ProductCategory } from './models/product';

@Component({
  selector: 'app-root',
  imports: [Encabezado, Portada, FiltroCategorias, CatalogoProductos, SobreNosotros, PiePagina],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  readonly categories = CATEGORIES;
  readonly selectedCategory = signal<ProductCategory | 'TODAS'>('TODAS');
  readonly visibleProducts = computed(() =>
    DEMO_PRODUCTS.filter(product => this.selectedCategory() === 'TODAS' || product.categoria === this.selectedCategory())
  );
}
