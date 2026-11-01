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

  it('no pide la foto al backend cuando el producto indica que no tiene', async () => {
    await TestBed.configureTestingModule({ imports: [ImagenProducto] }).compileComponents();
    const fixture = TestBed.createComponent(ImagenProducto);
    fixture.componentRef.setInput('producto', { id: 7, nombre: 'Limonada', descripcion: '', precio: 5, stock: 5, categoria: 'BEBIDAS', tieneImagen: false });
    fixture.detectChanges();
    const imagen = (fixture.nativeElement as HTMLElement).querySelector('img')!;
    expect(imagen.getAttribute('src')).toBe('/assets/demo/limonada.png');
  });

  it('pide la foto al backend después de cambiarla aunque el producto no tuviera', async () => {
    await TestBed.configureTestingModule({ imports: [ImagenProducto] }).compileComponents();
    const fixture = TestBed.createComponent(ImagenProducto);
    fixture.componentRef.setInput('producto', { id: 7, nombre: 'Limonada', descripcion: '', precio: 5, stock: 5, categoria: 'BEBIDAS', tieneImagen: false });
    fixture.componentRef.setInput('version', 1);
    fixture.detectChanges();
    const imagen = (fixture.nativeElement as HTMLElement).querySelector('img')!;
    expect(imagen.getAttribute('src')).toBe('/api/productos/7/imagen?v=1');
  });
});
