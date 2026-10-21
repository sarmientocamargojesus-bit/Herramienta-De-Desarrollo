import { TestBed } from '@angular/core/testing';
import { DEMO_PRODUCTS } from '../../data/demo-catalog';
import { CatalogoProductos } from './catalogo-productos';

describe('CatalogoProductos', () => {
  it('muestra los productos recibidos', async () => {
    await TestBed.configureTestingModule({ imports: [CatalogoProductos] }).compileComponents();
    const fixture = TestBed.createComponent(CatalogoProductos);
    fixture.componentRef.setInput('products', DEMO_PRODUCTS);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('app-tarjeta-producto').length).toBe(DEMO_PRODUCTS.length);
  });
});
