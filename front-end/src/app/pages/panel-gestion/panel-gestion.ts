import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CarritoService } from '../../services/carrito.service';
import { SesionService } from '../../services/sesion.service';
import { AvisoExito } from '../../shared/aviso-exito/aviso-exito';

interface Seccion { ruta: string; texto: string; icono: string; }

// Trazos SVG (24×24) de cada ícono del menú lateral.
const ICONOS = {
  resumen: 'M4 20V10M10 20V4M16 20v-7M22 20H2',
  pedidos: 'M6 3h12v18l-3-2-3 2-3-2-3 2V3zM9 8h6M9 12h6',
  inventario: 'M3 7l9-4 9 4-9 4-9-4zM3 7v10l9 4 9-4V7M12 11v10',
  productos: 'M3 12V4h8l10 10-8 8-10-10zM7.5 7.5h.01',
  categorias: 'M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z',
  pagos: 'M3 6h18v12H3zM3 10h18M7 15h4',
  usuarios: 'M16 19v-1a4 4 0 0 0-8 0v1M12 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM20 19v-1a3 3 0 0 0-2-2.8M17 5.2a3 3 0 0 1 0 5.6',
  tienda: 'M4 9l1-5h14l1 5M4 9h16v11H4zM9 20v-6h6v6',
  salir: 'M15 4h4v16h-4M10 8l-4 4 4 4M6 12h10',
};

@Component({ imports: [RouterOutlet, RouterLink, RouterLinkActive, AvisoExito], selector: 'app-panel-gestion', templateUrl: './panel-gestion.html', styleUrl: './panel-gestion.scss' })
export class PanelGestion {
  readonly sesion = inject(SesionService); private readonly carrito = inject(CarritoService); private readonly router = inject(Router);
  readonly iconos = ICONOS;
  // Operación: lo que comparten empleado y administrador. Administración: solo el administrador.
  readonly operacion: Seccion[] = [
    { ruta: '/gestion/resumen', texto: 'Resumen', icono: ICONOS.resumen },
    { ruta: '/gestion/pedidos', texto: 'Pedidos', icono: ICONOS.pedidos },
    { ruta: '/gestion/inventario', texto: 'Inventario', icono: ICONOS.inventario },
  ];
  readonly administracion: Seccion[] = [
    { ruta: '/gestion/productos', texto: 'Productos', icono: ICONOS.productos },
    { ruta: '/gestion/categorias', texto: 'Categorías', icono: ICONOS.categorias },
    { ruta: '/gestion/pagos', texto: 'Pagos', icono: ICONOS.pagos },
    { ruta: '/gestion/usuarios', texto: 'Usuarios', icono: ICONOS.usuarios },
  ];
  readonly esAdministrador = computed(() => this.sesion.rol() === 'ADMINISTRADOR');
  readonly correo = computed(() => this.sesion.sesion()?.correo ?? '');
  readonly inicial = computed(() => this.correo().charAt(0).toUpperCase() || '?');
  readonly rolTexto = computed(() => this.esAdministrador() ? 'Administrador' : 'Empleado');
  salir(): void { this.sesion.salir(); this.carrito.cargar().subscribe(); void this.router.navigateByUrl('/'); }
}
