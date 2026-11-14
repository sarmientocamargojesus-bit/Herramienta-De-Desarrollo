import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AccesoDenegado } from "./acceso-denegado";

describe("AccesoDenegado", () => {
  let component: AccesoDenegado;
  let fixture: ComponentFixture<AccesoDenegado>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccesoDenegado],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AccesoDenegado);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
