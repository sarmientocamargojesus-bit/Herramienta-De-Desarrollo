import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { DialogoConfirmacion } from "./dialogo-confirmacion";

describe("DialogoConfirmacion", () => {
  let component: DialogoConfirmacion;
  let fixture: ComponentFixture<DialogoConfirmacion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogoConfirmacion],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(DialogoConfirmacion);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
