import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { FormularioInventario } from "./formulario-inventario";

describe("FormularioInventario", () => {
  let component: FormularioInventario;
  let fixture: ComponentFixture<FormularioInventario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioInventario],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioInventario);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
