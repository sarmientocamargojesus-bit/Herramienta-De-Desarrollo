import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { PaginaNoEncontrada } from "./pagina-no-encontrada";

describe("PaginaNoEncontrada", () => {
  let component: PaginaNoEncontrada;
  let fixture: ComponentFixture<PaginaNoEncontrada>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginaNoEncontrada],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(PaginaNoEncontrada);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
