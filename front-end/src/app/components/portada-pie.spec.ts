import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Inicio } from '../pages/inicio/inicio';
import { PiePagina } from './pie-pagina/pie-pagina';
import { Portada } from './portada/portada';
import { ComoPedir } from './como-pedir/como-pedir';
import { Vitrina } from './vitrina/vitrina';

const SESION = 'fast-food-sesion';
const CARRITO = 'fast-food-carrito-invitado';

function iniciarSesion(rol: string): void {
  sessionStorage.setItem(SESION, JSON.stringify({
    token: 't', tipo: 'Bearer', expiraEnSegundos: 3600, correo: 'ana@correo.com', rol, venceEn: Date.now() + 3600000
  }));
}

describe('Portada y pie de página', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])] });
  });
  afterEach(() => { sessionStorage.removeItem(SESION); localStorage.removeItem(CARRITO); });

  it('"Ver combos" avisa la categoría y solo aparece si COMBOS está activa', () => {
    const fixture = TestBed.createComponent(Portada);
    fixture.componentRef.setInput('categorias', [{ label: 'Todos', value: 'TODAS' }, { label: 'BEBIDAS', value: 'BEBIDAS' }]);
    const elegidas: string[] = [];
    fixture.componentInstance.categoriaElegida.subscribe(c => elegidas.push(c));
    fixture.detectChanges();
    const portada = fixture.nativeElement as HTMLElement;
    expect(portada.querySelector('.hero-secundario')).toBeNull();

    fixture.componentRef.setInput('categorias', [{ label: 'Todos', value: 'TODAS' }, { label: 'COMBOS', value: 'COMBOS' }]);
    fixture.detectChanges();
    (portada.querySelector('.hero-secundario') as HTMLButtonElement).click();
    expect(elegidas).toEqual(['COMBOS']);
  });

  it('la vitrina arma categorías, selección variada, cifras y combos con el catálogo real', () => {
    const fixture = TestBed.createComponent(Vitrina);
    fixture.componentRef.setInput('categorias', [
      { label: 'Todos', value: 'TODAS' }, { label: 'COMBOS', value: 'COMBOS' }, { label: 'BEBIDAS', value: 'BEBIDAS' },
    ]);
    fixture.componentRef.setInput('productos', [
      { id: 1, nombre: 'Combo Doble', descripcion: 'Para dos', precio: 54.9, stock: 5, categoria: 'COMBOS', tieneImagen: false },
      { id: 2, nombre: 'Combo Personal', descripcion: '', precio: 19.9, stock: 0, categoria: 'COMBOS', tieneImagen: false },
      { id: 3, nombre: 'Combo Clásico', descripcion: '', precio: 29.9, stock: 4, categoria: 'COMBOS', tieneImagen: false },
      { id: 4, nombre: 'Cola', descripcion: '', precio: 6.9, stock: 9, categoria: 'BEBIDAS', tieneImagen: false },
    ]);
    const agregados: string[] = [];
    const elegidas: string[] = [];
    fixture.componentInstance.agregar.subscribe(p => agregados.push(p.nombre));
    fixture.componentInstance.categoriaElegida.subscribe(c => elegidas.push(c));
    fixture.detectChanges();
    const vitrina = fixture.componentInstance;

    expect(vitrina.tarjetas().map(t => [t.nombre, t.cantidad])).toEqual([['Combos', 3], ['Bebidas', 1]]);
    // Sin el agotado y alternando categorías.
    expect(vitrina.seleccion().map(p => p.nombre)).toEqual(['Combo Doble', 'Cola', 'Combo Clásico']);
    expect(vitrina.cifras().map(c => c.valor)).toEqual([4, 2, 3]);
    expect(vitrina.combos().map(p => p.nombre)).toEqual(['Combo Clásico', 'Combo Doble']);

    const html = fixture.nativeElement as HTMLElement;
    (html.querySelector('.categoria') as HTMLButtonElement).click();
    (html.querySelector('.mas') as HTMLButtonElement).click();
    expect(elegidas).toEqual(['COMBOS']);
    expect(agregados).toEqual(['Combo Doble']);
  });

  it('"Ver mi pedido" se oculta para el personal', () => {
    iniciarSesion('EMPLEADO');
    const fixture = TestBed.createComponent(ComoPedir);
    fixture.detectChanges();
    const html = fixture.nativeElement as HTMLElement;
    expect(html.querySelectorAll('.pasos li').length).toBe(3);
    expect(html.querySelector('.boton-borde')).toBeNull();
  });

  it('muestra el pedido en curso y el producto destacado', () => {
    localStorage.setItem(CARRITO, JSON.stringify([
      { productoId: 1, nombre: 'Burger', precio: 20, stock: 5, cantidad: 2, fechaAgregado: '2026-11-11T10:00:00' },
    ]));
    const fixture = TestBed.createComponent(Portada);
    fixture.componentRef.setInput('destacado', { id: 4, nombre: 'Combo Clásico', descripcion: '', precio: 29.9, stock: 5, categoria: 'COMBOS' });
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Tienes 2 productos en tu pedido');
    expect(texto).toContain('Combo Clásico');
    expect(texto).toContain('desde S/ 29.90');
  });

  it('Inicio destaca el combo disponible más económico y filtra al elegir una categoría', () => {
    const fixture = TestBed.createComponent(Inicio);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/productos').flush([
      { id: 1, nombre: 'Combo Doble', precio: 54.9, stock: 5, categoria: 'COMBOS', categoriaId: 2 },
      { id: 2, nombre: 'Combo Personal', precio: 29.9, stock: 0, categoria: 'COMBOS', categoriaId: 2 },
      { id: 3, nombre: 'Combo Clásico', precio: 32, stock: 4, categoria: 'COMBOS', categoriaId: 2 },
      { id: 4, nombre: 'Cola', precio: 6.9, stock: 9, categoria: 'BEBIDAS', categoriaId: 4 },
    ]);
    http.expectOne('/api/categorias/activas').flush([
      { id: 2, nombre: 'COMBOS', descripcion: '', activo: true }, { id: 4, nombre: 'BEBIDAS', descripcion: '', activo: true },
    ]);
    const inicio = fixture.componentInstance;
    // El de S/ 29.90 está agotado: se destaca el siguiente más económico con stock.
    expect(inicio.destacado()?.nombre).toBe('Combo Clásico');

    inicio.irACategoria('BEBIDAS');
    expect(inicio.visibles().map(p => p.nombre)).toEqual(['Cola']);
  });

  const enlacesCuenta = () => {
    const fixture = TestBed.createComponent(PiePagina);
    fixture.detectChanges();
    return [...(fixture.nativeElement as HTMLElement).querySelectorAll('nav[aria-label="Tu cuenta"] a')]
      .map(a => a.textContent?.trim());
  };

  it('el pie ofrece iniciar sesión o crear cuenta al visitante', () => {
    expect(enlacesCuenta()).toEqual(['Iniciar sesión', 'Crear cuenta', 'Mi pedido']);
  });

  it('el pie lleva al personal al panel de gestión', () => {
    iniciarSesion('ADMINISTRADOR');
    expect(enlacesCuenta()).toEqual(['Panel de gestión']);
  });
});
