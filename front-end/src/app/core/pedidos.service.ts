import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { CheckoutRequest, CheckoutResponse, DetallePedido, Pedido } from '../models/product';

@Injectable({ providedIn: 'root' })
export class PedidosService {
  private readonly http = inject(HttpClient);
  confirmar(datos: CheckoutRequest) { return this.http.post<CheckoutResponse>('/api/checkout', datos); }
  mios() { return this.http.get<Pedido[]>('/api/pedidos/mios'); }
  todos() { return this.http.get<Pedido[]>('/api/pedidos'); }
  obtener(id: number) { return this.http.get<Pedido>(`/api/pedidos/${id}`); }
  detalles(id: number) { return this.http.get<DetallePedido[]>(`/api/pedidos/${id}/detalles`); }
}
