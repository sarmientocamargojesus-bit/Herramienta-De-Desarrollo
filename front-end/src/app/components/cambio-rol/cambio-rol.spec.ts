import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { CambioRol } from "./cambio-rol";

describe("CambioRol", () => {
  let component: CambioRol;
  let fixture: ComponentFixture<CambioRol>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CambioRol],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(CambioRol);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
