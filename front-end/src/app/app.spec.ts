import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [App] }).compileComponents();
  });

  it('crea la vista del cliente', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const content = fixture.nativeElement as HTMLElement;
    expect(content.querySelector('h1')?.textContent).toContain('RÁPIDO.');
    expect(content.querySelectorAll('app-tarjeta-producto').length).toBe(4);
  });

  it('filtra los productos por categoría', () => {
    const fixture = TestBed.createComponent(App);
    fixture.componentInstance.selectedCategory.set('BEBIDAS');
    fixture.detectChanges();
    expect(fixture.componentInstance.visibleProducts().length).toBe(1);
    expect(fixture.componentInstance.visibleProducts()[0].nombre).toBe('Limonada Iqueña');
  });
});
