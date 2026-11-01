import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Producto } from '../models/product';
import { CarritoService } from './carrito.service';

const esperar = () => new Promise(resolver => setTimeout(resolver, 0));
const producto: Producto = { id: 7, nombre: 'Hamburguesa', descripcion: '', precio: 12.5, stock: 10, categoria: 'HAMBURGUESAS' };
const linea = (cantidad: number) => [{ productoId: 7, nombre: 'Hamburguesa', precio: 12.5, stock: 10, cantidad, fechaAgregado: '2026-11-01T10:00:00' }];

describe('CarritoService del cliente', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.setItem('fast-food-sesion', JSON.stringify({
      token: 'token-prueba', tipo: 'Bearer', expiraEnSegundos: 3600, correo: 'cliente@correo.com', rol: 'CLIENTE', venceEn: Date.now() + 3600000
    }));
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.removeItem('fast-food-sesion');
  });

  it('dos clics rápidos en Agregar guardan dos unidades', async () => {
    const carrito = TestBed.inject(CarritoService);
    carrito.agregar(producto).subscribe();
    carrito.agregar(producto).subscribe();
    await esperar();

    // Primer clic: lee el carrito vacío y guarda 1 unidad.
    http.expectOne({ method: 'GET', url: '/api/carrito' }).flush([]);
    await esperar();
    const primera = http.expectOne({ method: 'PUT', url: '/api/carrito/productos/7' });
    expect(primera.request.body.cantidad).toBe(1);
    primera.flush(linea(1)[0]);
    await esperar();
    http.expectOne({ method: 'GET', url: '/api/carrito' }).flush(linea(1));
    await esperar();

    // Segundo clic: comienza después del primero y parte de la unidad ya guardada.
    http.expectOne({ method: 'GET', url: '/api/carrito' }).flush(linea(1));
    await esperar();
    const segunda = http.expectOne({ method: 'PUT', url: '/api/carrito/productos/7' });
    expect(segunda.request.body.cantidad).toBe(2);
    segunda.flush(linea(2)[0]);
    await esperar();
    http.expectOne({ method: 'GET', url: '/api/carrito' }).flush(linea(2));
    await esperar();

    expect(carrito.items()[0].cantidad).toBe(2);
  });

  it('un error en una operación no bloquea la siguiente', async () => {
    const carrito = TestBed.inject(CarritoService);
    let fallo = false;
    carrito.quitar(7).subscribe({ error: () => fallo = true });
    carrito.agregar(producto).subscribe();
    await esperar();

    http.expectOne({ method: 'DELETE', url: '/api/carrito/productos/7' })
      .flush({ mensaje: 'Error' }, { status: 500, statusText: 'Error' });
    await esperar();
    expect(fallo).toBe(true);

    http.expectOne({ method: 'GET', url: '/api/carrito' }).flush([]);
    await esperar();
    http.expectOne({ method: 'PUT', url: '/api/carrito/productos/7' }).flush(linea(1)[0]);
    await esperar();
    http.expectOne({ method: 'GET', url: '/api/carrito' }).flush(linea(1));
    await esperar();
    expect(carrito.items()[0].cantidad).toBe(1);
  });
});
