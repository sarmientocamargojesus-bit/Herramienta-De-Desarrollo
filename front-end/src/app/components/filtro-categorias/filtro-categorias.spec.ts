import { TestBed } from '@angular/core/testing';
import { CATEGORIES } from '../../data/demo-catalog';
import { FiltroCategorias } from './filtro-categorias';

describe('FiltroCategorias', () => {
  it('muestra las categorías recibidas', async () => {
    await TestBed.configureTestingModule({ imports: [FiltroCategorias] }).compileComponents();
    const fixture = TestBed.createComponent(FiltroCategorias);
    fixture.componentRef.setInput('categories', CATEGORIES);
    fixture.componentRef.setInput('selected', 'TODAS');
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('button').length).toBe(CATEGORIES.length);
  });
});
