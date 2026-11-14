import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { MisPedidos } from "./mis-pedidos";

describe("MisPedidos", () => {
  let component: MisPedidos;
  let fixture: ComponentFixture<MisPedidos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisPedidos],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(MisPedidos);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
