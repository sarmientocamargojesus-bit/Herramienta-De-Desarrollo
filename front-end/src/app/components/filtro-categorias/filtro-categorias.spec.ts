import { TestBed } from '@angular/core/testing';
import { FiltroCategorias } from './filtro-categorias';

describe('FiltroCategorias', () => {
  const categorias = [{label:'Todos',value:'TODAS'},{label:'Bebidas',value:'BEBIDAS'}];
  it('muestra las categorías recibidas', async () => {
    await TestBed.configureTestingModule({ imports: [FiltroCategorias] }).compileComponents();
    const fixture = TestBed.createComponent(FiltroCategorias);
    fixture.componentRef.setInput('categories', categorias);
    fixture.componentRef.setInput('selected', 'TODAS');
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('button').length).toBe(categorias.length);
  });
});
