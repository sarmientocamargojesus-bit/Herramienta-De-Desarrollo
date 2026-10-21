import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { GestionPagos } from "./gestion-pagos";

describe("GestionPagos", () => {
  let component: GestionPagos;
  let fixture: ComponentFixture<GestionPagos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionPagos],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionPagos);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
