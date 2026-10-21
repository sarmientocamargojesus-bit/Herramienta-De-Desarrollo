import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { DetalleProducto } from "./detalle-producto";

describe("DetalleProducto", () => {
  let component: DetalleProducto;
  let fixture: ComponentFixture<DetalleProducto>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetalleProducto],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(DetalleProducto);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
