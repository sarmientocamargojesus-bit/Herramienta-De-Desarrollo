import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Inventario, Pago, Rol, Usuario } from '../models/product';
import { API_URL } from './api.config';

@Injectable({ providedIn: 'root' })
export class GestionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_URL);
  inventarios() { return this.http.get<Inventario[]>(`${this.api}/inventarios`); }
  inventario(id: number) { return this.http.get<Inventario>(`${this.api}/inventarios/${id}`); }
  actualizarMinimo(id: number, stockMinimo: number) { return this.http.put<Inventario>(`${this.api}/inventarios/${id}`, { stockMinimo }); }
  pagos() { return this.http.get<Pago[]>(`${this.api}/pagos`); }
  usuarios() { return this.http.get<Usuario[]>(`${this.api}/usuarios`); }
  cambiarRol(id: number, rol: Rol) { return this.http.put<Usuario>(`${this.api}/usuarios/${id}/rol`, { rol }); }
}
