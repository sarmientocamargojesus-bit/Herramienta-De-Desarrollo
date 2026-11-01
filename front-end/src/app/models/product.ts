export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  categoria: string;
  categoriaId?: number;
  tieneImagen?: boolean;
}

export interface Categoria { id: number; nombre: string; descripcion: string; activo: boolean; }
export type Rol = 'CLIENTE' | 'EMPLEADO' | 'ADMINISTRADOR';
export interface Usuario { id: number; nombre: string; correo: string; telefono: string; direccion: string | null; rol: Rol; activo: boolean; }
export interface CarritoItem { productoId: number; nombre: string; precio: number; stock: number; cantidad: number; fechaAgregado: string; }
export interface Pedido { id: number; usuario: Usuario; telefono: string; fecha: string; fechaActualizacion: string; estado: string; subtotal: number; costoEntrega: number; total: number; tipoEntrega: string; direccionEntrega: string | null; observaciones: string | null; }
export interface DetallePedido { id: number; producto: Producto; cantidad: number; precioUnitario: number; subtotal: number; }
export interface Pago { id: number; pedido: Pedido; metodo: string; estado: string; monto: number; fecha: string; }
export interface Inventario { id: number; producto: Producto; stockMinimo: number; fechaActualizacion: string; fechaStockBajo: string | null; fechaAgotado: string | null; }
export interface CheckoutRequest { tipoEntrega: 'RECOJO' | 'DOMICILIO'; direccionEntrega: string; observaciones: string; metodoPago: 'TARJETA' | 'YAPE'; referenciaPrueba: 'DEMO-APROBADO'; }
export interface CheckoutResponse { pedidoId: number; estadoPedido: string; pagoId: number; estadoPago: string; total: number; fechaPedido: string; }
