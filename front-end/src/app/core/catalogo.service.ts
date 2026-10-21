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
  actualizarProducto(id: number, datos: Omit<Producto, 'id' | 'categoriaId'>) { return this.http.put<Producto>(`/api/productos/${id}`, datos); }
  eliminarProducto(id: number) { return this.http.delete<void>(`/api/productos/${id}`); }
  crearCategoria(datos: Pick<Categoria, 'nombre' | 'descripcion' | 'activo'>) { return this.http.post<Categoria>('/api/categorias', datos); }
  actualizarCategoria(id: number, datos: Pick<Categoria, 'nombre' | 'descripcion' | 'activo'>) { return this.http.put<Categoria>(`/api/categorias/${id}`, datos); }
  eliminarCategoria(id: number) { return this.http.delete<void>(`/api/categorias/${id}`); }
}
