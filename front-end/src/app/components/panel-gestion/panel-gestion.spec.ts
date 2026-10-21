import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { PanelGestion } from "./panel-gestion";

describe("PanelGestion", () => {
  let component: PanelGestion;
  let fixture: ComponentFixture<PanelGestion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PanelGestion],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(PanelGestion);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
