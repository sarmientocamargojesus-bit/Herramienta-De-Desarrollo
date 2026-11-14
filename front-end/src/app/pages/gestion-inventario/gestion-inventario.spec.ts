import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { GestionInventario } from "./gestion-inventario";

describe("GestionInventario", () => {
  let component: GestionInventario;
  let fixture: ComponentFixture<GestionInventario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionInventario],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionInventario);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
