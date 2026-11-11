import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AvisosService } from '../core/avisos.service';
import { Producto } from '../models/product';
import { FormularioCategoria } from './formulario-categoria/formulario-categoria';
import { GestionProductos } from './gestion-productos/gestion-productos';

@Component({ template: '' })
class Destino {}

const papas: Producto = { id: 4, nombre: 'Papas', descripcion: '', precio: 6, stock: 3, categoria: 'PIQUEOS' };

describe('Avisos de éxito en gestión', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(),
        provideRouter([{ path: 'gestion/categorias', component: Destino }])],
    });
  });

  it('avisa que el producto se eliminó solo cuando la API lo confirma', () => {
    const fixture = TestBed.createComponent(GestionProductos);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/productos').flush([papas]);

    fixture.componentInstance.elegido.set(papas);
    fixture.componentInstance.eliminar();
    expect(TestBed.inject(AvisosService).mensaje()).toBe('');
    http.expectOne(req => req.method === 'DELETE').flush(null, { status: 204, statusText: 'No Content' });

    expect(TestBed.inject(AvisosService).mensaje()).toBe('Producto «Papas» eliminado.');
    http.expectOne('/api/productos').flush([]);
  });

  it('no avisa éxito si la API rechaza la eliminación', () => {
    const fixture = TestBed.createComponent(GestionProductos);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/productos').flush([papas]);

    fixture.componentInstance.elegido.set(papas);
    fixture.componentInstance.eliminar();
    http.expectOne(req => req.method === 'DELETE')
      .flush({ mensaje: 'La operación viola una restricción de datos' }, { status: 409, statusText: 'Conflict' });

    expect(TestBed.inject(AvisosService).mensaje()).toBe('');
    expect(fixture.componentInstance.error()).toBe('La operación viola una restricción de datos');
  });

  it('avisa que la categoría se guardó después de la respuesta de la API', () => {
    const fixture = TestBed.createComponent(FormularioCategoria);
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ nombre: 'Postres', descripcion: '', activo: true });
    fixture.componentInstance.guardar();
    expect(TestBed.inject(AvisosService).mensaje()).toBe('');

    TestBed.inject(HttpTestingController).expectOne('/api/categorias')
      .flush({ id: 9, nombre: 'POSTRES', descripcion: '', activo: true });
    expect(TestBed.inject(AvisosService).mensaje()).toBe('Categoría guardada.');
  });
});
