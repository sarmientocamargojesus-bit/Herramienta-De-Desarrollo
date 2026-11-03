import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { FormularioProducto } from "./formulario-producto";

describe("FormularioProducto", () => {
  let component: FormularioProducto;
  let fixture: ComponentFixture<FormularioProducto>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioProducto],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioProducto);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("muestra un mensaje específico en cada campo inválido", () => {
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/categorias/activas').flush([]);
    component.form.setValue({ nombre: '   ', descripcion: '', precio: 0, stock: 1.5, categoria: '' });
    component.guardar();
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Escribe el nombre del producto.');
    expect(texto).toContain('El precio debe ser mayor que 0.');
    expect(texto).toContain('El stock debe ser un número entero de 0 o más.');
    expect(texto).toContain('Selecciona una categoría.');
    TestBed.inject(HttpTestingController).expectNone('/api/productos');
  });
});
