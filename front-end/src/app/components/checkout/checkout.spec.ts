import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Checkout } from "./checkout";

describe("Checkout", () => {
  let component: Checkout;
  let fixture: ComponentFixture<Checkout>;

  beforeEach(async () => {
    localStorage.setItem('fast-food-carrito-invitado', JSON.stringify([
      { productoId: 1, nombre: 'Hamburguesa', precio: 12.5, stock: 5, cantidad: 1, fechaAgregado: '2026-11-01T10:00:00' }
    ]));
    await TestBed.configureTestingModule({
      imports: [Checkout],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
  });

  afterEach(() => localStorage.removeItem('fast-food-carrito-invitado'));

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("permite confirmar por recojo después de intentar domicilio sin dirección", async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    const elegirEntrega = async (valor: string) => {
      const select = fixture.nativeElement.querySelector('select[formControlName="tipoEntrega"]') as HTMLSelectElement;
      select.value = valor;
      select.dispatchEvent(new Event('change'));
      fixture.detectChanges();
      await fixture.whenStable();
    };

    await elegirEntrega('DOMICILIO');
    component.enviar();
    expect(component.error()).toContain('dirección');

    await elegirEntrega('RECOJO');
    expect(component.error()).toBe('');
    component.enviar();

    const peticion = TestBed.inject(HttpTestingController).expectOne('/api/checkout');
    expect(peticion.request.body.tipoEntrega).toBe('RECOJO');
    expect(component.error()).toBe('');
  });
});
