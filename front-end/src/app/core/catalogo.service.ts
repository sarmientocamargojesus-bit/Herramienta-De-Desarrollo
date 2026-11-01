import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Categoria, Producto } from '../models/product';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  productos() { return this.http.get<Producto[]>('/api/productos'); }
  producto(id: number) { return this.http.get<Producto>(`/api/productos/${id}`); }
  categorias() { return this.http.get<Categoria[]>('/api/categorias/activas'); }
  todasCategorias() { return this.http.get<Categoria[]>('/api/categorias'); }
  crearProducto(datos: Omit<Producto, 'id' | 'categoriaId'>) { return this.http.post<Producto>('/api/productos', datos); }
  // stockAnterior es el stock que se mostró al abrir el formulario; el backend rechaza la edición si una venta lo cambió.
  actualizarProducto(id: number, datos: Omit<Producto, 'id' | 'categoriaId'>, stockAnterior?: number) {
    const params = stockAnterior === undefined ? undefined : { stockAnterior };
    return this.http.put<Producto>(`/api/productos/${id}`, datos, { params });
  }
  eliminarProducto(id: number) { return this.http.delete<void>(`/api/productos/${id}`); }
  subirImagen(id: number, archivo: File) {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.put<void>(`/api/productos/${id}/imagen`, datos);
  }
  quitarImagen(id: number) { return this.http.delete<void>(`/api/productos/${id}/imagen`); }
  crearCategoria(datos: Pick<Categoria, 'nombre' | 'descripcion' | 'activo'>) { return this.http.post<Categoria>('/api/categorias', datos); }
  actualizarCategoria(id: number, datos: Pick<Categoria, 'nombre' | 'descripcion' | 'activo'>) { return this.http.put<Categoria>(`/api/categorias/${id}`, datos); }
  eliminarCategoria(id: number) { return this.http.delete<void>(`/api/categorias/${id}`); }
}
