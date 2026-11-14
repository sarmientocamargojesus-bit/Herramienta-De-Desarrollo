import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { ConfirmacionPedido } from "./confirmacion-pedido";

describe("ConfirmacionPedido", () => {
  let component: ConfirmacionPedido;
  let fixture: ComponentFixture<ConfirmacionPedido>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfirmacionPedido],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmacionPedido);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
