import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Categoria, Producto } from '../models/product';
import { API_URL } from './api.config';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_URL);
  productos() { return this.http.get<Producto[]>(`${this.api}/productos`); }
  producto(id: number) { return this.http.get<Producto>(`${this.api}/productos/${id}`); }
  buscar(nombre: string) { return this.http.get<Producto[]>(`${this.api}/productos/buscar`, { params: { nombre } }); }
  categorias() { return this.http.get<Categoria[]>(`${this.api}/categorias/activas`); }
  todasCategorias() { return this.http.get<Categoria[]>(`${this.api}/categorias`); }
  crearProducto(datos: Omit<Producto, 'id' | 'categoriaId'>) { return this.http.post<Producto>(`${this.api}/productos`, datos); }
  // stockAnterior es el stock que se mostró al abrir el formulario; el backend rechaza la edición si una venta lo cambió.
  actualizarProducto(id: number, datos: Omit<Producto, 'id' | 'categoriaId'>, stockAnterior?: number) {
    const params = stockAnterior === undefined ? undefined : { stockAnterior };
    return this.http.put<Producto>(`${this.api}/productos/${id}`, datos, { params });
  }
  eliminarProducto(id: number) { return this.http.delete<void>(`${this.api}/productos/${id}`); }
  subirImagen(id: number, archivo: File) {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.put<void>(`${this.api}/productos/${id}/imagen`, datos);
  }
  quitarImagen(id: number) { return this.http.delete<void>(`${this.api}/productos/${id}/imagen`); }
  crearCategoria(datos: Pick<Categoria, 'nombre' | 'descripcion' | 'activo'>) { return this.http.post<Categoria>(`${this.api}/categorias`, datos); }
  actualizarCategoria(id: number, datos: Pick<Categoria, 'nombre' | 'descripcion' | 'activo'>) { return this.http.put<Categoria>(`${this.api}/categorias/${id}`, datos); }
  eliminarCategoria(id: number) { return this.http.delete<void>(`${this.api}/categorias/${id}`); }
}
