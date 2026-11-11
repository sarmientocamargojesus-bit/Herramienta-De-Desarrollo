import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { DetallePedido } from "./detalle-pedido";

describe("DetallePedido", () => {
  let component: DetallePedido;
  let fixture: ComponentFixture<DetallePedido>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetallePedido],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'gestion/pedidos/:id', component: DetallePedido },
          { path: 'mis-pedidos/:id', component: DetallePedido },
        ]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DetallePedido);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("desde gestión regresa al listado de pedidos de gestión", async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/gestion/pedidos/5', DetallePedido);
    const enlace = harness.routeNativeElement!.querySelector('a') as HTMLAnchorElement;
    expect(enlace.getAttribute('href')).toBe('/gestion/pedidos');
    expect(enlace.textContent).toContain('Pedidos');
  });

  it("muestra la fecha del pedido en formato legible", async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/mis-pedidos/5', DetallePedido);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/pedidos/5').flush({
      id: 5, fecha: '2026-10-21T10:05:00', estado: 'CONFIRMADO', tipoEntrega: 'RECOJO',
      subtotal: 25, costoEntrega: 0, total: 25
    });
    http.expectOne('/api/pedidos/5/detalles').flush([]);
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('.subtitulo')!.textContent).toContain('21/10/2026 10:05');
  });

  it("en gestión muestra los datos del cliente y de la entrega", async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/gestion/pedidos/7', DetallePedido);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/pedidos/7').flush({
      id: 7, fecha: '2026-11-01T12:00:00', estado: 'CONFIRMADO', tipoEntrega: 'DOMICILIO',
      direccionEntrega: 'Av. Lima 123', observaciones: 'Sin cebolla', telefono: '999111222',
      subtotal: 25, costoEntrega: 0, total: 25,
      usuario: { nombre: 'Ana Pérez', correo: 'ana@correo.com', telefono: '988' }
    });
    http.expectOne('/api/pedidos/7/detalles').flush([]);
    harness.detectChanges();
    const texto = harness.routeNativeElement!.textContent ?? '';
    for (const dato of ['Ana Pérez', 'ana@correo.com', '999111222', 'A domicilio', 'Av. Lima 123', 'Sin cebolla', 'S/ 25.00']) {
      expect(texto).toContain(dato);
    }
  });

  it("el cliente ve su entrega pero no el bloque de datos del cliente", async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/mis-pedidos/8', DetallePedido);
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/pedidos/8').flush({
      id: 8, fecha: '2026-11-01T12:00:00', estado: 'CONFIRMADO', tipoEntrega: 'RECOJO', observaciones: null,
      subtotal: 10, costoEntrega: 0, total: 10, usuario: { nombre: 'Ana', correo: 'ana@correo.com' }
    });
    http.expectOne('/api/pedidos/8/detalles').flush([]);
    harness.detectChanges();
    const texto = harness.routeNativeElement!.textContent ?? '';
    expect(texto).toContain('Recojo en tienda');
    expect(texto).toContain('Sin observaciones');
    expect(texto).not.toContain('Cliente');
  });

  it("desde la cuenta del cliente regresa a mis pedidos", async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/mis-pedidos/5', DetallePedido);
    const enlace = harness.routeNativeElement!.querySelector('a') as HTMLAnchorElement;
    expect(enlace.getAttribute('href')).toBe('/mis-pedidos');
    expect(enlace.textContent).toContain('Mis pedidos');
  });
});
