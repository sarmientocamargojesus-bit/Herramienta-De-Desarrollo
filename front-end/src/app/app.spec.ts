import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { provideRouter } from '@angular/router';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [App], providers: [provideRouter([])] }).compileComponents();
  });

  it('contiene el punto de entrada de rutas', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const content = fixture.nativeElement as HTMLElement;
    expect(content.querySelector('router-outlet')).toBeTruthy();
  });
});
