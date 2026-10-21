import { TestBed } from '@angular/core/testing';
import { ImagenProducto } from './imagen-producto';

describe('ImagenProducto', () => {
  it('usa la ilustración cuando el backend no tiene foto', async () => {
    await TestBed.configureTestingModule({ imports: [ImagenProducto] }).compileComponents();
    const fixture = TestBed.createComponent(ImagenProducto);
    fixture.componentRef.setInput('producto', { id: 7, nombre: 'Hamburguesa', descripcion: '', precio: 20, stock: 5, categoria: 'HAMBURGUESAS' });
    fixture.detectChanges();
    const imagen = (fixture.nativeElement as HTMLElement).querySelector('img')!;
    expect(imagen.getAttribute('src')).toContain('/api/productos/7/imagen');
    imagen.dispatchEvent(new Event('error'));
    fixture.detectChanges();
    expect(imagen.getAttribute('src')).toContain('/assets/demo/hero-fast-food.png');
  });
});
