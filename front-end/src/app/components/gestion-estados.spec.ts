import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { GestionCategorias } from './gestion-categorias/gestion-categorias';
import { GestionInventario } from './gestion-inventario/gestion-inventario';
import { GestionPagos } from './gestion-pagos/gestion-pagos';
import { GestionPedidos } from './gestion-pedidos/gestion-pedidos';
import { GestionProductos } from './gestion-productos/gestion-productos';
import { GestionUsuarios } from './gestion-usuarios/gestion-usuarios';

const pantallas: [string, Type<unknown>, string, string, object][] = [
  ['Pedidos', GestionPedidos, '/api/pedidos', 'Todavía no hay pedidos registrados.',
    { id: 1, usuario: { nombre: 'Ana' }, fecha: '2026-11-01T10:00:00', estado: 'CONFIRMADO', total: 25 }],
  ['Inventario', GestionInventario, '/api/inventarios', 'No hay productos con configuración de inventario.',
    { id: 1, producto: { nombre: 'Papas', stock: 3 }, stockMinimo: 5, fechaStockBajo: null, fechaAgotado: null }],
  ['Productos', GestionProductos, '/api/productos', 'Todavía no hay productos.',
    { id: 1, nombre: 'Papas', categoria: 'PIQUEOS', precio: 6, stock: 3 }],
  ['Categorías', GestionCategorias, '/api/categorias', 'Todavía no hay categorías.',
    { id: 1, nombre: 'PIQUEOS', descripcion: '', activo: true }],
  ['Pagos', GestionPagos, '/api/pagos', 'Todavía no hay pagos registrados.',
    { id: 1, pedido: { id: 1 }, metodo: 'YAPE', estado: 'APROBADO', monto: 25, fecha: '2026-11-01T10:00:00' }],
  ['Usuarios', GestionUsuarios, '/api/usuarios', 'No hay usuarios registrados.',
    { id: 1, nombre: 'Ana', correo: 'ana@correo.com', telefono: '999', rol: 'CLIENTE' }],
];

describe.each(pantallas)('Gestión de %s', (_nombre, componente, url, mensajeVacio, registro) => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [componente],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  function abrir() {
    const fixture = TestBed.createComponent(componente);
    fixture.detectChanges();
    const texto = () => { fixture.detectChanges(); return (fixture.nativeElement as HTMLElement).textContent ?? ''; };
    const tabla = () => { fixture.detectChanges(); return (fixture.nativeElement as HTMLElement).querySelector('table'); };
    return { http: TestBed.inject(HttpTestingController), texto, tabla };
  }

  it('muestra el indicador mientras carga', () => {
    const { http, texto, tabla } = abrir();
    expect(texto()).toContain('Cargando…');
    expect(tabla()).toBeNull();
    http.expectOne(url).flush([]);
  });

  it('muestra un mensaje cuando no hay registros', () => {
    const { http, texto, tabla } = abrir();
    http.expectOne(url).flush([]);
    expect(texto()).toContain(mensajeVacio);
    expect(texto()).not.toContain('Cargando…');
    expect(tabla()).toBeNull();
  });

  it('muestra la tabla cuando llegan datos', () => {
    const { http, texto, tabla } = abrir();
    http.expectOne(url).flush([registro]);
    expect(tabla()).not.toBeNull();
    expect(texto()).not.toContain(mensajeVacio);
  });

  it('muestra solo el error si la petición falla', () => {
    const { http, texto, tabla } = abrir();
    http.expectOne(url).flush({ mensaje: 'Error del servidor' }, { status: 500, statusText: 'Error' });
    expect(texto()).toContain('Error del servidor');
    expect(texto()).not.toContain('Cargando…');
    expect(texto()).not.toContain(mensajeVacio);
    expect(tabla()).toBeNull();
  });
});
