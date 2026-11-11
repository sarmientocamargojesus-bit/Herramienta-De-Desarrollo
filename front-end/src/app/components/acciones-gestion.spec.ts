import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { AvisosService } from '../core/avisos.service';
import { CambioRol } from './cambio-rol/cambio-rol';
import { GestionPagos } from './gestion-pagos/gestion-pagos';
import { GestionUsuarios } from './gestion-usuarios/gestion-usuarios';

@Component({ template: '' })
class Destino {}

const CORREO_SESION = 'admin@fastfood.test';
const usuario = (id: number, nombre: string, correo: string, rol: string, activo = true) =>
  ({ id, nombre, correo, telefono: '999111222', direccion: 'Av. Lima', rol, activo });

describe('Acciones del administrador', () => {
  beforeEach(() => {
    sessionStorage.setItem('fast-food-sesion', JSON.stringify({
      token: 'token-prueba', tipo: 'Bearer', expiraEnSegundos: 3600, correo: CORREO_SESION, rol: 'ADMINISTRADOR', venceEn: Date.now() + 3600000
    }));
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([
        { path: 'gestion/usuarios', component: Destino },
        { path: 'gestion/usuarios/:id/rol', component: CambioRol },
        { path: 'iniciar-sesion', component: Destino },
      ])],
    });
  });

  afterEach(() => sessionStorage.removeItem('fast-food-sesion'));

  it('anular un pago pide confirmación, llama a DELETE y avisa cuando la API responde', () => {
    const fixture = TestBed.createComponent(GestionPagos);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    const pago = { id: 9, pedido: { id: 4 }, metodo: 'YAPE', estado: 'APROBADO', monto: 25, fecha: '2026-11-01T10:00:00' };
    http.expectOne('/api/pagos').flush([pago]);

    fixture.componentInstance.elegido.set(pago as never);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('el pedido #4 quedará CANCELADO');
    http.expectNone(req => req.method === 'DELETE');

    fixture.componentInstance.anular();
    http.expectOne(req => req.method === 'DELETE' && req.url === '/api/pagos/9').flush(null, { status: 204, statusText: 'No Content' });
    expect(TestBed.inject(AvisosService).mensaje()).toBe('Pago #9 anulado. El pedido #4 quedó cancelado.');
    http.expectOne('/api/pagos').flush([]);
  });

  it('desactivar una cuenta reenvía sus datos con activo en false y sin contraseña', () => {
    const fixture = TestBed.createComponent(GestionUsuarios);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    const ana = usuario(2, 'Ana', 'ana@correo.com', 'CLIENTE');
    http.expectOne('/api/usuarios').flush([usuario(1, 'Admin', CORREO_SESION, 'ADMINISTRADOR'), ana]);

    const usuarios = fixture.componentInstance;
    expect(usuarios.esPropio(usuarios.usuarios()[0])).toBe(true);
    expect(usuarios.esPropio(usuarios.usuarios()[1])).toBe(false);
    fixture.detectChanges();
    const botones = [...(fixture.nativeElement as HTMLElement).querySelectorAll('button')].filter(b => b.textContent?.trim() === 'Desactivar');
    expect(botones[0].disabled).toBe(true);
    expect(botones[1].disabled).toBe(false);

    usuarios.elegido.set(usuarios.usuarios()[1]);
    usuarios.cambiarActivo();
    const peticion = http.expectOne(req => req.method === 'PUT' && req.url === '/api/usuarios/2');
    expect(peticion.request.body).toEqual({ nombre: 'Ana', correo: 'ana@correo.com', telefono: '999111222', direccion: 'Av. Lima', activo: false });
    peticion.flush({ ...ana, activo: false });
    expect(TestBed.inject(AvisosService).mensaje()).toBe('Cuenta de Ana desactivada.');
    http.expectOne('/api/usuarios').flush([]);
  });

  it('el cambio de rol se envía solo después de confirmar', async () => {
    const harness = await RouterTestingHarness.create();
    const rol = await harness.navigateByUrl('/gestion/usuarios/2/rol', CambioRol);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/usuarios').flush([usuario(2, 'Ana', 'ana@correo.com', 'CLIENTE')]);

    rol.guardar();
    expect(rol.error()).toBe('El usuario ya tiene ese rol.');
    expect(rol.confirmando()).toBe(false);

    rol.form.setValue({ rol: 'EMPLEADO' });
    rol.guardar();
    expect(rol.confirmando()).toBe(true);
    expect(rol.mensajeConfirmacion()).toContain('pasará de CLIENTE a EMPLEADO');
    http.expectNone(req => req.method === 'PUT');

    rol.confirmar();
    http.expectOne(req => req.method === 'PUT' && req.url === '/api/usuarios/2/rol').flush(usuario(2, 'Ana', 'ana@correo.com', 'EMPLEADO'));
    expect(TestBed.inject(AvisosService).mensaje()).toBe('Rol actualizado.');
  });

  it('advierte cuando el administrador cambia su propio rol', async () => {
    const harness = await RouterTestingHarness.create();
    const rol = await harness.navigateByUrl('/gestion/usuarios/1/rol', CambioRol);
    TestBed.inject(HttpTestingController).expectOne('/api/usuarios').flush([usuario(1, 'Admin', CORREO_SESION, 'ADMINISTRADOR')]);
    harness.detectChanges();

    expect(rol.esPropio()).toBe(true);
    expect(harness.routeNativeElement!.textContent).toContain('Es tu propia cuenta');
    rol.form.setValue({ rol: 'CLIENTE' });
    expect(rol.mensajeConfirmacion()).toContain('perderás el acceso a Gestión');
  });
});
