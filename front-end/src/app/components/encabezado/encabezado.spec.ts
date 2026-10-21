import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { Encabezado } from './encabezado';

describe('Encabezado', () => {
  let component: Encabezado;
  let fixture: ComponentFixture<Encabezado>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Encabezado],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Encabezado);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
