import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
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

  it("desde la cuenta del cliente regresa a mis pedidos", async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/mis-pedidos/5', DetallePedido);
    const enlace = harness.routeNativeElement!.querySelector('a') as HTMLAnchorElement;
    expect(enlace.getAttribute('href')).toBe('/mis-pedidos');
    expect(enlace.textContent).toContain('Mis pedidos');
  });
});
