import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { GestionCategorias } from "./gestion-categorias";

describe("GestionCategorias", () => {
  let component: GestionCategorias;
  let fixture: ComponentFixture<GestionCategorias>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionCategorias],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionCategorias);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
