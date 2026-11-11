import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CheckoutRequest, CheckoutResponse, DetallePedido, Pedido } from '../models/product';
import { API_URL } from './api.config';

@Injectable({ providedIn: 'root' })
export class PedidosService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_URL);
  confirmar(datos: CheckoutRequest) { return this.http.post<CheckoutResponse>(`${this.api}/checkout`, datos); }
  mios() { return this.http.get<Pedido[]>(`${this.api}/pedidos/mios`); }
  todos() { return this.http.get<Pedido[]>(`${this.api}/pedidos`); }
  obtener(id: number) { return this.http.get<Pedido>(`${this.api}/pedidos/${id}`); }
  detalles(id: number) { return this.http.get<DetallePedido[]>(`${this.api}/pedidos/${id}/detalles`); }
  totalPorEstado(estado: string) { return this.http.get<number>(`${this.api}/pedidos/total-por-estado/${estado}`); }
}
