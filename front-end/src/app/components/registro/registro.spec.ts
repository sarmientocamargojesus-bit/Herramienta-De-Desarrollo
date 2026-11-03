import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Registro } from "./registro";

describe("Registro", () => {
  let component: Registro;
  let fixture: ComponentFixture<Registro>;
  const datosValidos = {
    nombre: 'Ana Pérez', correo: 'ana@correo.com', telefono: '999111222', direccion: '',
    contrasena: 'secreta1', confirmarContrasena: 'secreta1'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Registro],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Registro);
    component = fixture.componentInstance;
  });

  const texto = () => (fixture.nativeElement as HTMLElement).textContent ?? '';

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("no envía el registro si las contraseñas no coinciden", () => {
    component.formulario.setValue({ ...datosValidos, confirmarContrasena: 'distinta1' });
    component.enviar();
    fixture.detectChanges();

    TestBed.inject(HttpTestingController).expectNone('/api/usuarios');
    expect(texto()).toContain('Las contraseñas no coinciden.');
  });

  it("rechaza un nombre formado solo por espacios", () => {
    component.formulario.setValue({ ...datosValidos, nombre: '   ' });
    component.enviar();
    fixture.detectChanges();

    TestBed.inject(HttpTestingController).expectNone('/api/usuarios');
    expect(component.formulario.controls.nombre.errors).toEqual({ soloEspacios: true });
    expect(texto()).toContain('Ingresa tu nombre.');
  });

  it("envía los datos sin la confirmación de contraseña", () => {
    component.formulario.setValue(datosValidos);
    component.enviar();

    const peticion = TestBed.inject(HttpTestingController).expectOne('/api/usuarios');
    expect(peticion.request.body).toEqual({
      nombre: 'Ana Pérez', correo: 'ana@correo.com', telefono: '999111222', direccion: '', contrasena: 'secreta1'
    });
  });

  it("muestra el correo duplicado junto al campo y conserva los datos", () => {
    fixture.detectChanges(); // La página ya está visible cuando el usuario envía el formulario.
    component.formulario.setValue(datosValidos);
    component.enviar();
    TestBed.inject(HttpTestingController).expectOne('/api/usuarios').flush(
      { mensaje: 'Ya existe un usuario con el correo: ana@correo.com' },
      { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();

    expect(component.formulario.controls.correo.errors).toEqual({ registrado: true });
    expect(texto()).toContain('Este correo ya está registrado.');
    expect(component.error()).toBe('');
    expect(component.formulario.controls.nombre.value).toBe('Ana Pérez');
    expect(component.enviando()).toBe(false);
  });
});
