import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { from, lastValueFrom, Observable, of, switchMap, tap } from 'rxjs';
import { CarritoItem, Producto } from '../models/product';
import { API_URL } from './api.config';
import { SesionService } from './sesion.service';

const CLAVE = 'fast-food-carrito-invitado';

function fechaLocal(): string {
  const ahora = new Date();
  const digitos = (n: number) => String(n).padStart(2, '0');
  return `${ahora.getFullYear()}-${digitos(ahora.getMonth() + 1)}-${digitos(ahora.getDate())}T${digitos(ahora.getHours())}:${digitos(ahora.getMinutes())}:${digitos(ahora.getSeconds())}`;
}

function leerInvitado(): CarritoItem[] {
  try {
    const datos = JSON.parse(localStorage.getItem(CLAVE) || '[]') as CarritoItem[];
    return Array.isArray(datos) ? datos.filter(item => Number.isInteger(item.productoId) && item.cantidad > 0) : [];
  } catch { return []; }
}

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_URL);
  private readonly sesion = inject(SesionService);
  readonly items = signal<CarritoItem[]>(leerInvitado());

  private ultimaOperacion: Promise<unknown> = Promise.resolve();

  constructor() {
    // Si la sesión del cliente termina (salida, vencimiento o 401), se muestra el carrito del navegador.
    effect(() => { if (!this.esCliente()) this.items.set(leerInvitado()); });
  }

  private esCliente(): boolean { return this.sesion.rol() === 'CLIENTE'; }
  // Los cambios del carrito del cliente se ejecutan uno después de otro: así cada clic parte de la
  // cantidad ya guardada por el anterior. Un error no detiene las operaciones siguientes.
  private enOrden<T>(operacion: () => Observable<T>): Observable<T> {
    const ejecucion = this.ultimaOperacion.catch(() => undefined).then(() => lastValueFrom(operacion()));
    this.ultimaOperacion = ejecucion;
    return from(ejecucion);
  }
  private guardarInvitado(items: CarritoItem[]): void {
    localStorage.setItem(CLAVE, JSON.stringify(items));
    this.items.set(items);
  }

  cargar(): Observable<CarritoItem[]> {
    if (!this.esCliente()) {
      const items = leerInvitado();
      this.items.set(items);
      return of(items);
    }
    return this.http.get<CarritoItem[]>(`${this.api}/carrito`).pipe(tap(items => this.items.set(items)));
  }

  agregar(producto: Producto, cantidad = 1): Observable<CarritoItem[]> {
    if (producto.stock < cantidad || cantidad < 1) throw new Error('El producto no tiene stock suficiente.');
    if (this.esCliente()) {
      return this.enOrden(() => this.cargar().pipe(
        switchMap(items => {
          const actual = items.find(item => item.productoId === producto.id)?.cantidad ?? 0;
          return this.http.put<CarritoItem>(`${this.api}/carrito/productos/${producto.id}`, { productoId: producto.id, cantidad: actual + cantidad });
        }),
        switchMap(() => this.cargar())
      ));
    }
    const actuales = leerInvitado();
    const anterior = actuales.find(item => item.productoId === producto.id);
    const nuevaCantidad = (anterior?.cantidad ?? 0) + cantidad;
    if (nuevaCantidad > Math.min(100, producto.stock)) throw new Error('No hay stock suficiente para esa cantidad.');
    const nuevos = anterior
      ? actuales.map(item => item.productoId === producto.id ? { ...item, cantidad: nuevaCantidad } : item)
      : [...actuales, { productoId: producto.id, nombre: producto.nombre, precio: producto.precio, stock: producto.stock, cantidad, fechaAgregado: fechaLocal() }];
    this.guardarInvitado(nuevos);
    return of(nuevos);
  }

  establecer(item: CarritoItem, cantidad: number): Observable<CarritoItem[]> {
    if (!Number.isInteger(cantidad) || cantidad < 1 || cantidad > Math.min(100, item.stock)) throw new Error('La cantidad supera el stock disponible.');
    if (this.esCliente()) return this.enOrden(() => this.http.put<CarritoItem>(`${this.api}/carrito/productos/${item.productoId}`, { productoId: item.productoId, cantidad })
      .pipe(switchMap(() => this.cargar())));
    const nuevos = leerInvitado().map(actual => actual.productoId === item.productoId ? { ...actual, cantidad } : actual);
    this.guardarInvitado(nuevos);
    return of(nuevos);
  }

  quitar(productoId: number): Observable<CarritoItem[]> {
    if (this.esCliente()) return this.enOrden(() => this.http.delete<void>(`${this.api}/carrito/productos/${productoId}`)
      .pipe(switchMap(() => this.cargar())));
    const nuevos = leerInvitado().filter(item => item.productoId !== productoId);
    this.guardarInvitado(nuevos);
    return of(nuevos);
  }

  importarInvitado(): Observable<CarritoItem[]> {
    const invitado = leerInvitado();
    if (!invitado.length || !this.esCliente()) return of([]);
    const datos = invitado.map(({ productoId, cantidad, fechaAgregado }) => ({ productoId, cantidad, fechaAgregado }));
    return this.http.post<CarritoItem[]>(`${this.api}/carrito/importar`, datos).pipe(tap(items => {
      localStorage.removeItem(CLAVE);
      this.items.set(items);
    }));
  }
}
