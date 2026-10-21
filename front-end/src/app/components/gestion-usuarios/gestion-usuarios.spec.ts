import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { GestionUsuarios } from "./gestion-usuarios";

describe("GestionUsuarios", () => {
  let component: GestionUsuarios;
  let fixture: ComponentFixture<GestionUsuarios>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionUsuarios],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(GestionUsuarios);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
