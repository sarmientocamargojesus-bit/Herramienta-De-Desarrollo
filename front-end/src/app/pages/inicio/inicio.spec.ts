import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Producto } from '../../models/product';
import { Inicio } from "./inicio";

const hamburguesa: Producto = { id: 1, nombre: 'Hamburguesa clásica', descripcion: '', precio: 12.5, stock: 5, categoria: 'HAMBURGUESAS', tieneImagen: false };
const papas: Producto = { id: 2, nombre: 'Papas fritas', descripcion: '', precio: 6, stock: 5, categoria: 'PIQUEOS', tieneImagen: false };

describe("Inicio", () => {
  let component: Inicio;
  let fixture: ComponentFixture<Inicio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inicio],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Inicio);
    component = fixture.componentInstance;
  });

  afterEach(() => vi.useRealTimers());

  function cargarMenu(http: HttpTestingController): void {
    fixture.detectChanges();
    http.expectOne('/api/productos').flush([hamburguesa, papas]);
    http.expectOne('/api/categorias/activas').flush([]);
  }

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("espera a que se deje de escribir y busca una sola vez", () => {
    vi.useFakeTimers();
    const http = TestBed.inject(HttpTestingController);
    cargarMenu(http);

    component.busqueda.setValue('ham');
    vi.advanceTimersByTime(100);
    component.busqueda.setValue('hamb');
    vi.advanceTimersByTime(300);

    const peticion = http.expectOne(req => req.url === '/api/productos/buscar');
    expect(peticion.request.params.get('nombre')).toBe('hamb');
    expect(component.buscando()).toBe(true);
    peticion.flush([hamburguesa]);

    expect(component.buscando()).toBe(false);
    expect(component.visibles()).toEqual([hamburguesa]);
  });

  it("cancela la búsqueda anterior cuando cambia el texto", () => {
    vi.useFakeTimers();
    const http = TestBed.inject(HttpTestingController);
    cargarMenu(http);

    component.busqueda.setValue('ham');
    vi.advanceTimersByTime(300);
    const anterior = http.expectOne(req => req.params.get('nombre') === 'ham');
    component.busqueda.setValue('papas');
    vi.advanceTimersByTime(300);
    const actual = http.expectOne(req => req.params.get('nombre') === 'papas');

    expect(anterior.cancelled).toBe(true);
    actual.flush([papas]);
    expect(component.visibles()).toEqual([papas]);
  });

  it("al borrar la búsqueda vuelve a mostrar todo el menú", () => {
    vi.useFakeTimers();
    const http = TestBed.inject(HttpTestingController);
    cargarMenu(http);

    component.busqueda.setValue('papas');
    vi.advanceTimersByTime(300);
    http.expectOne(req => req.url === '/api/productos/buscar').flush([papas]);
    component.busqueda.setValue('  ');
    vi.advanceTimersByTime(300);

    http.expectNone(req => req.url === '/api/productos/buscar');
    expect(component.visibles()).toEqual([hamburguesa, papas]);
  });
});
