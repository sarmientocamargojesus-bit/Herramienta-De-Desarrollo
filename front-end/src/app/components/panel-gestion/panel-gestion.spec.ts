import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { PanelGestion } from "./panel-gestion";

function iniciarSesion(rol: string): void {
  sessionStorage.setItem('fast-food-sesion', JSON.stringify({
    token: 'token-prueba', tipo: 'Bearer', expiraEnSegundos: 3600, correo: 'gestion@fastfood.test', rol, venceEn: Date.now() + 3600000
  }));
}

describe("PanelGestion", () => {
  let component: PanelGestion;
  let fixture: ComponentFixture<PanelGestion>;

  async function crear(rol: string): Promise<HTMLElement> {
    iniciarSesion(rol);
    await TestBed.configureTestingModule({
      imports: [PanelGestion],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(PanelGestion);
    component = fixture.componentInstance;
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  afterEach(() => sessionStorage.removeItem('fast-food-sesion'));

  const enlaces = (raiz: HTMLElement) => [...raiz.querySelectorAll('nav a')].map(a => a.textContent?.trim());

  it("should create", async () => {
    await crear('ADMINISTRADOR');
    expect(component).toBeTruthy();
  });

  it("muestra la cuenta en sesión y las dos secciones al administrador", async () => {
    const raiz = await crear('ADMINISTRADOR');
    expect(raiz.querySelector('.correo')?.textContent).toBe('gestion@fastfood.test');
    expect(raiz.querySelector('.rol')?.textContent).toBe('Administrador');
    expect([...raiz.querySelectorAll('.grupo')].map(g => g.textContent)).toEqual(['Operación', 'Administración']);
    expect(enlaces(raiz)).toEqual(['Resumen', 'Pedidos', 'Inventario', 'Productos', 'Categorías', 'Pagos', 'Usuarios']);
    expect(raiz.querySelectorAll('nav a svg').length).toBe(7);
  });

  it("al empleado solo le muestra la sección de operación", async () => {
    const raiz = await crear('EMPLEADO');
    expect(raiz.querySelector('.rol')?.textContent).toBe('Empleado');
    expect([...raiz.querySelectorAll('.grupo')].map(g => g.textContent)).toEqual(['Operación']);
    expect(enlaces(raiz)).toEqual(['Resumen', 'Pedidos', 'Inventario']);
  });
});
