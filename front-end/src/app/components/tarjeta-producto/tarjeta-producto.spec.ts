import { TestBed } from '@angular/core/testing';
import { DEMO_PRODUCTS } from '../../data/demo-catalog';
import { TarjetaProducto } from './tarjeta-producto';

describe('TarjetaProducto', () => {
  it('muestra el nombre del producto recibido', async () => {
    await TestBed.configureTestingModule({ imports: [TarjetaProducto] }).compileComponents();
    const fixture = TestBed.createComponent(TarjetaProducto);
    fixture.componentRef.setInput('product', DEMO_PRODUCTS[0]);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('h3')?.textContent).toContain(DEMO_PRODUCTS[0].nombre);
  });
});
