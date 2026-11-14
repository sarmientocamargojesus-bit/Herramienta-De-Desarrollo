import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { DisenoCliente } from "./diseno-cliente";

describe("DisenoCliente", () => {
  let component: DisenoCliente;
  let fixture: ComponentFixture<DisenoCliente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DisenoCliente],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(DisenoCliente);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
