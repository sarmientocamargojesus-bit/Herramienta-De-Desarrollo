import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { AvisosService } from '../services/avisos.service';
import { CarritoService } from '../services/carrito.service';
import { Checkout } from './checkout/checkout';
import { DetalleProducto } from './detalle-producto/detalle-producto';
import { InicioSesion } from './inicio-sesion/inicio-sesion';
import { MisPedidos } from './mis-pedidos/mis-pedidos';
import { Registro } from './registro/registro';
import { SelectorCantidad } from '../shared/selector-cantidad/selector-cantidad';

@Component({ template: '' })
class Destino {}

const CARRITO = 'fast-food-carrito-invitado';

describe('Mejoras de la tienda', () => {
  beforeEach(() => {
    localStorage.removeItem(CARRITO);
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([
        { path: 'productos/:id', component: DetalleProducto },
        { path: 'iniciar-sesion', component: InicioSesion },
        { path: 'registro', component: Registro },
        { path: 'carrito', component: Destino },
      ])],
    });
  });

  afterEach(() => localStorage.removeItem(CARRITO));

  it('el selector de cantidad respeta el mínimo y el máximo', () => {
    const fixture = TestBed.createComponent(SelectorCantidad);
    const emitidos: number[] = [];
    fixture.componentInstance.valorCambio.subscribe(v => emitidos.push(v));
    fixture.componentRef.setInput('valor', 1);
    fixture.componentRef.setInput('maximo', 2);
    fixture.detectChanges();
    const [menos, mas] = (fixture.nativeElement as HTMLElement).querySelectorAll('button');
    expect(menos.disabled).toBe(true);
    mas.click();
    fixture.componentRef.setInput('valor', 2);
    fixture.detectChanges();
    expect(mas.disabled).toBe(true);
    expect(emitidos).toEqual([2]);
  });

  it('el detalle agrega la cantidad elegida y muestra el aviso con enlace al carrito', async () => {
    const harness = await RouterTestingHarness.create();
    const detalle = await harness.navigateByUrl('/productos/3', DetalleProducto);
    TestBed.inject(HttpTestingController).expectOne('/api/productos/3').flush(
      { id: 3, nombre: 'Papas Fuego', descripcion: '', precio: 12.5, stock: 10, categoria: 'PIQUEOS', tieneImagen: false });
    harness.detectChanges();

    detalle.cantidad.set(3);
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).toContain('Agregar 3 al carrito');
    detalle.agregar();

    expect(TestBed.inject(CarritoService).items()[0].cantidad).toBe(3);
    expect(TestBed.inject(AvisosService).mensaje()).toBe('3 × Papas Fuego se agregó al carrito.');
    expect(TestBed.inject(AvisosService).enlace()).toEqual({ texto: 'Ver carrito', ruta: '/carrito' });
    expect(detalle.cantidad()).toBe(1);
  });

  it('después de registrarse va al login con el correo escrito, sin ponerlo en la URL', async () => {
    const harness = await RouterTestingHarness.create();
    const registro = await harness.navigateByUrl('/registro', Registro);
    registro.formulario.setValue({
      nombre: 'Ana', correo: 'Ana@Correo.com', telefono: '999111222', direccion: '', contrasena: 'secreta1', confirmarContrasena: 'secreta1'
    });
    registro.enviar();
    TestBed.inject(HttpTestingController).expectOne('/api/usuarios').flush({ id: 5 });
    await harness.fixture.whenStable();
    harness.detectChanges();

    const router = TestBed.inject(Router);
    expect(router.url).toBe('/iniciar-sesion');
    const login = harness.routeDebugElement!.componentInstance as InicioSesion;
    expect(login.formulario.controls.correo.value).toBe('ana@correo.com');
    expect(harness.routeNativeElement!.textContent).toContain('Cuenta creada');
  });

  it('el login permite mostrar la contraseña', async () => {
    const harness = await RouterTestingHarness.create();
    const login = await harness.navigateByUrl('/iniciar-sesion', InicioSesion);
    const campo = () => harness.routeNativeElement!.querySelector('input[formcontrolname="contrasena"]') as HTMLInputElement;
    expect(campo().type).toBe('password');
    login.verContrasena.set(true);
    harness.detectChanges();
    expect(campo().type).toBe('text');
  });

  it('el checkout muestra el total del carrito antes de confirmar', () => {
    localStorage.setItem(CARRITO, JSON.stringify([
      { productoId: 1, nombre: 'Burger', precio: 24.9, stock: 5, cantidad: 2, fechaAgregado: '2026-11-11T10:00:00' },
      { productoId: 2, nombre: 'Cola', precio: 6.5, stock: 5, cantidad: 1, fechaAgregado: '2026-11-11T10:00:00' },
    ]));
    const fixture = TestBed.createComponent(Checkout);
    fixture.detectChanges();
    expect(fixture.componentInstance.total()).toBeCloseTo(56.3);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('S/ 56.30');
  });

  it('Mis pedidos muestra primero el pedido más reciente', () => {
    const fixture = TestBed.createComponent(MisPedidos);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/pedidos/mios').flush([
      { id: 1, fecha: '2026-11-01T10:00:00', estado: 'CONFIRMADO', total: 10 },
      { id: 2, fecha: '2026-11-05T10:00:00', estado: 'CANCELADO', total: 20 },
    ]);
    expect(fixture.componentInstance.ordenados().map(p => p.id)).toEqual([2, 1]);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('.etiqueta').length).toBe(2);
  });
});
