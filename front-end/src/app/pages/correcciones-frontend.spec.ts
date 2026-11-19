import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { CarritoService } from '../services/carrito.service';
import { correoConDominio } from '../shared/validadores';
import { CambioRol } from './cambio-rol/cambio-rol';
import { DetallePedido } from './detalle-pedido/detalle-pedido';
import { DetalleProducto } from './detalle-producto/detalle-producto';
import { GestionPagos } from './gestion-pagos/gestion-pagos';
import { GestionPedidos } from './gestion-pedidos/gestion-pedidos';
import { Inicio } from './inicio/inicio';

@Component({ template: '' })
class Destino {}

const SESION = 'fast-food-sesion';
const CARRITO = 'fast-food-carrito-invitado';
const CORREO_ADMIN = 'admin@fastfood.test';

function iniciarSesion(rol: string): void {
  sessionStorage.setItem(SESION, JSON.stringify({
    token: 'token-prueba', tipo: 'Bearer', expiraEnSegundos: 3600, correo: CORREO_ADMIN, rol, venceEn: Date.now() + 3600000
  }));
}

const pedido = (id: number, estado: string) => ({
  id, fecha: '2026-11-10T10:00:00', estado, tipoEntrega: 'RECOJO', subtotal: 25, costoEntrega: 0, total: 25,
  usuario: { nombre: 'Ana', correo: 'ana@correo.com' }
});

describe('Correcciones del frontend', () => {
  beforeEach(() => {
    localStorage.removeItem(CARRITO);
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([
        { path: 'gestion/pedidos/:id', component: DetallePedido },
        { path: 'mis-pedidos/:id', component: DetallePedido },
        { path: 'productos/:id', component: DetalleProducto },
        { path: 'gestion/usuarios/:id/rol', component: CambioRol },
        { path: 'gestion/usuarios', component: Destino },
        { path: 'carrito', component: Destino },
      ])],
    });
  });

  afterEach(() => { sessionStorage.removeItem(SESION); localStorage.removeItem(CARRITO); });

  it('en Gestión el detalle avanza el pedido al estado siguiente', async () => {
    iniciarSesion('EMPLEADO');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/gestion/pedidos/5', DetallePedido);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/pedidos/5').flush(pedido(5, 'CONFIRMADO'));
    http.expectOne('/api/pedidos/5/detalles').flush([]);
    harness.detectChanges();

    const boton = harness.routeNativeElement!.querySelector('.avance button') as HTMLButtonElement;
    expect(boton.textContent).toContain('Pasar a preparación');
    boton.click();
    const peticion = http.expectOne({ method: 'PUT', url: '/api/pedidos/5/estado' });
    expect(peticion.request.body).toEqual({ estado: 'EN_PREPARACION' });
    peticion.flush(pedido(5, 'EN_PREPARACION'));
    harness.detectChanges();

    const texto = harness.routeNativeElement!.textContent ?? '';
    expect(texto).toContain('En preparación');
    expect(texto).toContain('Marcar como listo');
  });

  it('el cliente y los pedidos entregados no muestran el botón de avance', async () => {
    iniciarSesion('CLIENTE');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/mis-pedidos/6', DetallePedido);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/pedidos/6').flush(pedido(6, 'CONFIRMADO'));
    http.expectOne('/api/pedidos/6/detalles').flush([]);
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('.avance')).toBeNull();

    await harness.navigateByUrl('/gestion/pedidos/7', DetallePedido);
    http.expectOne('/api/pedidos/7').flush(pedido(7, 'ENTREGADO'));
    http.expectOne('/api/pedidos/7/detalles').flush([]);
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('.avance')).toBeNull();
  });

  it('el filtro de pedidos incluye los estados nuevos con nombres legibles', () => {
    const fixture = TestBed.createComponent(GestionPedidos);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/pedidos').flush([pedido(1, 'LISTO')]);
    fixture.detectChanges();
    const opciones = [...(fixture.nativeElement as HTMLElement).querySelectorAll('select option')].map(o => o.textContent?.trim());
    expect(opciones).toEqual(expect.arrayContaining(['En preparación', 'Listo', 'Entregado']));
  });

  it('el menú oculta los productos de categorías inactivas', () => {
    const fixture = TestBed.createComponent(Inicio);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/productos').flush([
      { id: 1, nombre: 'Burger', descripcion: '', precio: 20, stock: 5, categoria: 'HAMBURGUESAS', categoriaId: 1 },
      { id: 2, nombre: 'Postre retirado', descripcion: '', precio: 8, stock: 5, categoria: 'POSTRES', categoriaId: 9 },
    ]);
    http.expectOne('/api/categorias/activas').flush([{ id: 1, nombre: 'HAMBURGUESAS', descripcion: '', activo: true }]);
    expect(fixture.componentInstance.visibles().map(p => p.nombre)).toEqual(['Burger']);
  });

  it('el detalle de un producto de categoría inactiva no ofrece agregarlo', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/productos/2', DetalleProducto);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/productos/2').flush(
      { id: 2, nombre: 'Postre retirado', descripcion: '', precio: 8, stock: 5, categoria: 'POSTRES', categoriaId: 9 });
    http.expectOne('/api/categorias/activas').flush([{ id: 1, nombre: 'HAMBURGUESAS', descripcion: '', activo: true }]);
    harness.detectChanges();
    const texto = harness.routeNativeElement!.textContent ?? '';
    expect(texto).toContain('no está disponible');
    expect(texto).not.toContain('Agregar al carrito');
  });

  it('un pago de un pedido entregado no se puede anular', () => {
    iniciarSesion('ADMINISTRADOR');
    const fixture = TestBed.createComponent(GestionPagos);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/pagos').flush([
      { id: 1, metodo: 'YAPE', estado: 'APROBADO', monto: 25, fecha: '2026-11-10T10:00:00', pedido: pedido(10, 'ENTREGADO') },
      { id: 2, metodo: 'YAPE', estado: 'APROBADO', monto: 25, fecha: '2026-11-11T10:00:00', pedido: pedido(11, 'LISTO') },
    ]);
    fixture.detectChanges();
    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.querySelectorAll('button.peligro').length).toBe(1);
    expect(elemento.textContent).toContain('Pedido entregado');
  });

  it('un administrador no puede cambiar su propio rol', async () => {
    iniciarSesion('ADMINISTRADOR');
    const harness = await RouterTestingHarness.create();
    const pagina = await harness.navigateByUrl('/gestion/usuarios/1/rol', CambioRol);
    TestBed.inject(HttpTestingController).expectOne('/api/usuarios').flush([
      { id: 1, nombre: 'Admin', correo: CORREO_ADMIN, telefono: '999', direccion: null, rol: 'ADMINISTRADOR', activo: true }
    ]);
    harness.detectChanges();
    const boton = harness.routeNativeElement!.querySelector('button.primario') as HTMLButtonElement;
    expect(boton.disabled).toBe(true);
    expect(pagina.form.controls.rol.disabled).toBe(true);
    pagina.guardar();
    expect(pagina.confirmando()).toBe(false);
  });

  it('el carrito del visitante se actualiza con el precio vigente y quita productos borrados', () => {
    localStorage.setItem(CARRITO, JSON.stringify([
      { productoId: 1, nombre: 'Burger', precio: 20, stock: 5, cantidad: 2, fechaAgregado: '2026-11-11T10:00:00' },
      { productoId: 2, nombre: 'Borrado', precio: 5, stock: 5, cantidad: 1, fechaAgregado: '2026-11-11T10:00:00' },
    ]));
    const carrito = TestBed.inject(CarritoService);
    carrito.cargar().subscribe();
    TestBed.inject(HttpTestingController).expectOne('/api/productos').flush([
      { id: 1, nombre: 'Burger Brava', precio: 24.9, stock: 3, categoria: 'HAMBURGUESAS' },
    ]);
    expect(carrito.items()).toEqual([
      { productoId: 1, nombre: 'Burger Brava', precio: 24.9, stock: 3, cantidad: 2, fechaAgregado: '2026-11-11T10:00:00' },
    ]);
    expect(JSON.parse(localStorage.getItem(CARRITO)!)).toHaveLength(1);
  });

  it('el correo exige un dominio con punto, igual que el backend', () => {
    expect(correoConDominio(new FormControl('ana@correo'))).toEqual({ correoIncompleto: true });
    expect(correoConDominio(new FormControl('ana@correo.com'))).toBeNull();
    expect(correoConDominio(new FormControl(''))).toBeNull(); // El vacío lo informa required.
  });
});
