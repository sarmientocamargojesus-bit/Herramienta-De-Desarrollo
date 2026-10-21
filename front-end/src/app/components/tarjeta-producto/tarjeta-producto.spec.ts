import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { TarjetaProducto } from './tarjeta-producto';

describe('TarjetaProducto', () => {
  const producto = {id:1,nombre:'Hamburguesa',descripcion:'Prueba',precio:20,stock:3,categoria:'HAMBURGUESAS'};
  it('muestra el nombre del producto recibido', async () => {
    await TestBed.configureTestingModule({ imports: [TarjetaProducto], providers:[provideRouter([])] }).compileComponents();
    const fixture = TestBed.createComponent(TarjetaProducto);
    fixture.componentRef.setInput('product', producto);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('h3')?.textContent).toContain(producto.nombre);
  });
});
