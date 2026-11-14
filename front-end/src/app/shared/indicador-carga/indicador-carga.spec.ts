import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { IndicadorCarga } from "./indicador-carga";

describe("IndicadorCarga", () => {
  let component: IndicadorCarga;
  let fixture: ComponentFixture<IndicadorCarga>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IndicadorCarga],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(IndicadorCarga);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
