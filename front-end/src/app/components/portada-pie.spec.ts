import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Inicio } from '../pages/inicio/inicio';
import { PiePagina } from './pie-pagina/pie-pagina';
import { Portada } from './portada/portada';

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

  it('los accesos directos avisan qué categoría se eligió', () => {
    const fixture = TestBed.createComponent(Portada);
    fixture.componentRef.setInput('categorias', [
      { label: 'Todos', value: 'TODAS' }, { label: 'COMBOS', value: 'COMBOS' }, { label: 'BEBIDAS', value: 'BEBIDAS' },
    ]);
    const elegidas: string[] = [];
    fixture.componentInstance.categoriaElegida.subscribe(c => elegidas.push(c));
    fixture.detectChanges();

    const botones = [...(fixture.nativeElement as HTMLElement).querySelectorAll('.accesos button')] as HTMLButtonElement[];
    expect(botones.map(b => b.textContent?.trim())).toEqual(['Combos', 'Bebidas']);
    botones[0].click();
    expect(elegidas).toEqual(['COMBOS']);
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
