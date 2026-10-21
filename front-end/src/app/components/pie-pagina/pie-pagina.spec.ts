import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { PiePagina } from './pie-pagina';

describe('PiePagina', () => {
  let component: PiePagina;
  let fixture: ComponentFixture<PiePagina>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PiePagina],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(PiePagina);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
