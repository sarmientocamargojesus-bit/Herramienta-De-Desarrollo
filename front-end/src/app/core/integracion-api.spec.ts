import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CatalogoService } from './catalogo.service';
import { GestionService } from './gestion.service';
import { PedidosService } from './pedidos.service';

describe('Contratos HTTP del frontend', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('consulta productos reales del backend', () => {
    TestBed.inject(CatalogoService).productos().subscribe(productos => expect(productos.length).toBe(1));
    const peticion = http.expectOne('/api/productos');
    expect(peticion.request.method).toBe('GET');
    peticion.flush([{ id: 1, nombre: 'Prueba', descripcion: '', precio: 10, stock: 2, categoria: 'COMBOS' }]);
  });

  it('envía la foto del producto como multipart al backend', () => {
    const archivo = new File(['contenido'], 'foto.png', { type: 'image/png' });
    TestBed.inject(CatalogoService).subirImagen(7, archivo).subscribe();
    const peticion = http.expectOne('/api/productos/7/imagen');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body instanceof FormData).toBe(true);
    expect((peticion.request.body as FormData).get('archivo')).toBe(archivo);
    peticion.flush(null);
  });

  it('confirma el pedido en un solo endpoint sin guardar datos de pago reales', () => {
    TestBed.inject(PedidosService).confirmar({ tipoEntrega: 'RECOJO', direccionEntrega: '', observaciones: '', metodoPago: 'YAPE', referenciaPrueba: 'DEMO-APROBADO' }).subscribe();
    const peticion = http.expectOne('/api/checkout');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body.referenciaPrueba).toBe('DEMO-APROBADO');
    expect(peticion.request.body.numeroTarjeta).toBeUndefined();
    peticion.flush({ pedidoId: 1, estadoPedido: 'CONFIRMADO', pagoId: 1, estadoPago: 'APROBADO', total: 10, fechaPedido: '2026-10-21T10:00:00' });
  });

  it('cambia solo el rol del usuario', () => {
    TestBed.inject(GestionService).cambiarRol(3, 'ADMINISTRADOR').subscribe();
    const peticion = http.expectOne('/api/usuarios/3/rol');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual({ rol: 'ADMINISTRADOR' });
    peticion.flush({ id: 3, nombre: 'Cliente', correo: 'cliente@example.com', telefono: '1234567', direccion: '', rol: 'ADMINISTRADOR', activo: true });
  });
});
