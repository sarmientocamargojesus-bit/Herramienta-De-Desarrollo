import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Inventario, Pago, Rol, Usuario } from '../models/product';
import { API_URL } from './api.config';

@Injectable({ providedIn: 'root' })
export class GestionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_URL);
  inventarios() { return this.http.get<Inventario[]>(`${this.api}/inventarios`); }
  inventariosBajoMinimo() { return this.http.get<Inventario[]>(`${this.api}/inventarios/bajo-minimo`); }
  inventario(id: number) { return this.http.get<Inventario>(`${this.api}/inventarios/${id}`); }
  actualizarMinimo(id: number, stockMinimo: number) { return this.http.put<Inventario>(`${this.api}/inventarios/${id}`, { stockMinimo }); }
  pagos() { return this.http.get<Pago[]>(`${this.api}/pagos`); }
  usuarios() { return this.http.get<Usuario[]>(`${this.api}/usuarios`); }
  // Anular elimina el pago: el backend devuelve el stock y deja el pedido CANCELADO.
  anularPago(id: number) { return this.http.delete<void>(`${this.api}/pagos/${id}`); }
  // Se reenvían los datos actuales para que el backend solo cambie el estado de la cuenta.
  cambiarActivo(u: Usuario, activo: boolean) {
    return this.http.put<Usuario>(`${this.api}/usuarios/${u.id}`, { nombre: u.nombre, correo: u.correo, telefono: u.telefono, direccion: u.direccion, activo });
  }
  cambiarRol(id: number, rol: Rol) { return this.http.put<Usuario>(`${this.api}/usuarios/${id}/rol`, { rol }); }
}
