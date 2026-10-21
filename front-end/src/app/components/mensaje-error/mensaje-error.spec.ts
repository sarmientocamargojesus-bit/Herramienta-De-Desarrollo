import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { MensajeError } from "./mensaje-error";

describe("MensajeError", () => {
  let component: MensajeError;
  let fixture: ComponentFixture<MensajeError>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MensajeError],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(MensajeError);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
