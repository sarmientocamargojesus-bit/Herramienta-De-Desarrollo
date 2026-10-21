import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { GestionPedidos } from "./gestion-pedidos";

describe("GestionPedidos", () => {
  let component: GestionPedidos;
  let fixture: ComponentFixture<GestionPedidos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionPedidos],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionPedidos);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
