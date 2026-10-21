import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { DetallePedido } from "./detalle-pedido";

describe("DetallePedido", () => {
  let component: DetallePedido;
  let fixture: ComponentFixture<DetallePedido>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetallePedido],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(DetallePedido);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
