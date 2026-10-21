import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Inventario, Pago, Rol, Usuario } from '../models/product';

@Injectable({ providedIn: 'root' })
export class GestionService {
  private readonly http = inject(HttpClient);
  inventarios() { return this.http.get<Inventario[]>('/api/inventarios'); }
  inventario(id: number) { return this.http.get<Inventario>(`/api/inventarios/${id}`); }
  actualizarMinimo(id: number, stockMinimo: number) { return this.http.put<Inventario>(`/api/inventarios/${id}`, { stockMinimo }); }
  pagos() { return this.http.get<Pago[]>('/api/pagos'); }
  usuarios() { return this.http.get<Usuario[]>('/api/usuarios'); }
  cambiarRol(id: number, rol: Rol) { return this.http.put<Usuario>(`/api/usuarios/${id}/rol`, { rol }); }
}
