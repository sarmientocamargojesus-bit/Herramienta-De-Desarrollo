import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { FormularioProducto } from "./formulario-producto";

describe("FormularioProducto", () => {
  let component: FormularioProducto;
  let fixture: ComponentFixture<FormularioProducto>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioProducto],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioProducto);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
