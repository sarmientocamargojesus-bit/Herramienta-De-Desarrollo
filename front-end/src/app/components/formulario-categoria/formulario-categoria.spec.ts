import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { FormularioCategoria } from "./formulario-categoria";

describe("FormularioCategoria", () => {
  let component: FormularioCategoria;
  let fixture: ComponentFixture<FormularioCategoria>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormularioCategoria],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(FormularioCategoria);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
