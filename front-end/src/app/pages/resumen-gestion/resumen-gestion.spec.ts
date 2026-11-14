import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ResumenGestion } from './resumen-gestion';

const pedido = (id: number, estado: string, fecha: string, total: number) =>
  ({ id, estado, fecha, total, usuario: { nombre: `Cliente ${id}` } });
const inventario = (id: number, nombre: string, stock: number) =>
  ({ id, producto: { nombre, stock }, stockMinimo: 5, fechaStockBajo: null, fechaAgotado: null });

describe('ResumenGestion', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ResumenGestion],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  function abrir() {
    const fixture = TestBed.createComponent(ResumenGestion);
    fixture.detectChanges();
    const texto = () => { fixture.detectChanges(); return (fixture.nativeElement as HTMLElement).textContent ?? ''; };
    return { fixture, http: TestBed.inject(HttpTestingController), texto };
  }

  it('calcula los indicadores con los datos de la API', () => {
    const { fixture, http, texto } = abrir();
    expect(texto()).toContain('Cargando…');

    http.expectOne('/api/pedidos').flush([
      pedido(1, 'CONFIRMADO', '2026-11-01T10:00:00', 25),
      pedido(2, 'CANCELADO', '2026-11-02T10:00:00', 12),
      pedido(3, 'CONFIRMADO', '2026-11-03T10:00:00', 40),
    ]);
    http.expectOne('/api/pedidos/total-por-estado/CONFIRMADO').flush(65);
    http.expectOne('/api/inventarios/bajo-minimo').flush([inventario(7, 'Papas', 3), inventario(8, 'Gaseosa', 0)]);

    const resumen = fixture.componentInstance;
    expect(resumen.totalVendido()).toBe(65);
    expect(resumen.confirmados()).toBe(2);
    expect(resumen.cancelados()).toBe(1);
    expect(resumen.agotados()).toBe(1);
    // Más recientes primero y lo más urgente de reponer primero.
    expect(resumen.ultimos().map(p => p.id)).toEqual([3, 2, 1]);
    expect(resumen.reponer().map(i => i.producto.nombre)).toEqual(['Gaseosa', 'Papas']);
    expect(texto()).toContain('S/ 65.00');
    expect(texto()).toContain('Agotado');
    expect(texto()).toContain('Stock bajo');
  });

  it('muestra mensajes cuando no hay pedidos ni productos por reponer', () => {
    const { http, texto } = abrir();
    http.expectOne('/api/pedidos').flush([]);
    http.expectOne('/api/pedidos/total-por-estado/CONFIRMADO').flush(0);
    http.expectOne('/api/inventarios/bajo-minimo').flush([]);

    expect(texto()).toContain('S/ 0.00');
    expect(texto()).toContain('Todavía no hay pedidos registrados.');
    expect(texto()).toContain('Ningún producto está en su stock mínimo.');
  });

  it('si falla una consulta muestra el error y permite reintentar', () => {
    const { fixture, http, texto } = abrir();
    http.expectOne('/api/pedidos').flush(null, { status: 0, statusText: 'Unknown Error' });
    http.match(() => true).forEach(peticion => expect(peticion.cancelled).toBe(true));

    expect(texto()).toContain('No se pudo conectar con el backend');
    expect(texto()).not.toContain('Total vendido');
    expect(fixture.componentInstance.reintentable()).toBe(true);
  });
});
