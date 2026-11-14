import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { GestionProductos } from "./gestion-productos";

describe("GestionProductos", () => {
  let component: GestionProductos;
  let fixture: ComponentFixture<GestionProductos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionProductos],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionProductos);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
