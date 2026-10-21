import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { Inicio } from "./inicio";

describe("Inicio", () => {
  let component: Inicio;
  let fixture: ComponentFixture<Inicio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inicio],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Inicio);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
