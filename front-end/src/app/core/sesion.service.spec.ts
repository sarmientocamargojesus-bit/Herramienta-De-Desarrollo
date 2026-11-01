import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { autenticacionInterceptor } from './autenticacion.interceptor';
import { CarritoService } from './carrito.service';
import { SesionService } from './sesion.service';

const CLAVE_SESION = 'fast-food-sesion';
const CLAVE_CARRITO = 'fast-food-carrito-invitado';

function guardarSesion(venceEn: number): void {
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify({
    token: 'token-prueba', tipo: 'Bearer', expiraEnSegundos: 60, correo: 'cliente@correo.com', rol: 'CLIENTE', venceEn
  }));
}

describe('SesionService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([autenticacionInterceptor])), provideHttpClientTesting()]
    });
  });

  afterEach(() => {
    vi.useRealTimers();
    sessionStorage.removeItem(CLAVE_SESION);
    localStorage.removeItem(CLAVE_CARRITO);
  });

  it('cierra la sesión en la página cuando vence el token', () => {
    vi.useFakeTimers();
    guardarSesion(Date.now() + 60_000);
    const sesion = TestBed.inject(SesionService);
    expect(sesion.autenticado()).toBe(true);

    vi.advanceTimersByTime(60_000);

    expect(sesion.autenticado()).toBe(false);
    expect(sesion.rol()).toBeNull();
    expect(sessionStorage.getItem(CLAVE_SESION)).toBeNull();
  });

  it('cierra la sesión cuando el backend rechaza el token con 401', () => {
    guardarSesion(Date.now() + 60_000);
    const sesion = TestBed.inject(SesionService);
    let estado = 0;
    TestBed.inject(HttpClient).get('/api/pedidos/mios').subscribe({ error: error => estado = error.status });

    const peticion = TestBed.inject(HttpTestingController).expectOne('/api/pedidos/mios');
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer token-prueba');
    peticion.flush({ mensaje: 'La sesión ya no es válida' }, { status: 401, statusText: 'Unauthorized' });

    expect(estado).toBe(401);
    expect(sesion.autenticado()).toBe(false);
  });

  it('un 403 no cierra la sesión', () => {
    guardarSesion(Date.now() + 60_000);
    const sesion = TestBed.inject(SesionService);
    TestBed.inject(HttpClient).get('/api/inventarios').subscribe({ error: () => undefined });

    TestBed.inject(HttpTestingController).expectOne('/api/inventarios')
      .flush({ mensaje: 'Sin permiso' }, { status: 403, statusText: 'Forbidden' });

    expect(sesion.autenticado()).toBe(true);
  });

  it('al cerrar la sesión el carrito vuelve a ser el del navegador', () => {
    vi.useFakeTimers();
    guardarSesion(Date.now() + 60_000);
    localStorage.setItem(CLAVE_CARRITO, '[]');
    const carrito = TestBed.inject(CarritoService);
    carrito.items.set([{ productoId: 1, nombre: 'Hamburguesa', precio: 10, stock: 5, cantidad: 3, fechaAgregado: '2026-11-01T10:00:00' }]);

    vi.advanceTimersByTime(60_000);
    TestBed.tick();

    expect(carrito.items()).toEqual([]);
  });
});
