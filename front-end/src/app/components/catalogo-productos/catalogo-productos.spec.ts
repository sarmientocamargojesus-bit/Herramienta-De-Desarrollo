import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CatalogoProductos } from './catalogo-productos';

describe('CatalogoProductos', () => {
  const productos = [{id:1,nombre:'Hamburguesa',descripcion:'Prueba',precio:20,stock:3,categoria:'HAMBURGUESAS'}];
  it('muestra los productos recibidos', async () => {
    await TestBed.configureTestingModule({ imports: [CatalogoProductos], providers:[provideRouter([])] }).compileComponents();
    const fixture = TestBed.createComponent(CatalogoProductos);
    fixture.componentRef.setInput('products', productos);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelectorAll('app-tarjeta-producto').length).toBe(productos.length);
  });
});
