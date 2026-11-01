import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { InicioSesion } from "./inicio-sesion";

describe("InicioSesion", () => {
  let component: InicioSesion;
  let fixture: ComponentFixture<InicioSesion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InicioSesion],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(InicioSesion);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("indica credenciales incorrectas en lugar de sesión vencida", () => {
    component.formulario.setValue({ correo: 'cliente@correo.com', contrasena: 'incorrecta' });
    component.enviar();

    TestBed.inject(HttpTestingController).expectOne('/api/auth/login').flush(
      { codigo: 401, mensaje: 'El correo o la contraseña son incorrectos' },
      { status: 401, statusText: 'Unauthorized' });

    expect(component.error()).toBe('El correo o la contraseña son incorrectos.');
    expect(component.enviando()).toBe(false);
  });
});
