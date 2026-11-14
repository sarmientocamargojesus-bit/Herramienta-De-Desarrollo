import { Routes } from '@angular/router';
import { rolGuard } from './guards/rol.guard';

export const routes: Routes = [
  {
    path: 'gestion',
    loadComponent: () => import('./pages/panel-gestion/panel-gestion').then(m => m.PanelGestion),
    canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR', 'EMPLEADO'] },
    children: [
      { path: '', redirectTo: 'resumen', pathMatch: 'full' },
      { path: 'resumen', loadComponent: () => import('./pages/resumen-gestion/resumen-gestion').then(m => m.ResumenGestion) },
      { path: 'productos', loadComponent: () => import('./pages/gestion-productos/gestion-productos').then(m => m.GestionProductos), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'productos/nuevo', loadComponent: () => import('./pages/formulario-producto/formulario-producto').then(m => m.FormularioProducto), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'productos/:id/editar', loadComponent: () => import('./pages/formulario-producto/formulario-producto').then(m => m.FormularioProducto), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'categorias', loadComponent: () => import('./pages/gestion-categorias/gestion-categorias').then(m => m.GestionCategorias), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'categorias/nueva', loadComponent: () => import('./pages/formulario-categoria/formulario-categoria').then(m => m.FormularioCategoria), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'categorias/:id/editar', loadComponent: () => import('./pages/formulario-categoria/formulario-categoria').then(m => m.FormularioCategoria), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'inventario', loadComponent: () => import('./pages/gestion-inventario/gestion-inventario').then(m => m.GestionInventario) },
      { path: 'inventario/:id/editar', loadComponent: () => import('./pages/formulario-inventario/formulario-inventario').then(m => m.FormularioInventario) },
      { path: 'pedidos', loadComponent: () => import('./pages/gestion-pedidos/gestion-pedidos').then(m => m.GestionPedidos) },
      { path: 'pedidos/:id', loadComponent: () => import('./pages/detalle-pedido/detalle-pedido').then(m => m.DetallePedido) },
      { path: 'pagos', loadComponent: () => import('./pages/gestion-pagos/gestion-pagos').then(m => m.GestionPagos), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'usuarios', loadComponent: () => import('./pages/gestion-usuarios/gestion-usuarios').then(m => m.GestionUsuarios), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'usuarios/:id/rol', loadComponent: () => import('./pages/cambio-rol/cambio-rol').then(m => m.CambioRol), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
    ]
  },
  {
    path: '',
    loadComponent: () => import('./pages/diseno-cliente/diseno-cliente').then(m => m.DisenoCliente),
    children: [
      { path: '', loadComponent: () => import('./pages/inicio/inicio').then(m => m.Inicio) },
      { path: 'productos/:id', loadComponent: () => import('./pages/detalle-producto/detalle-producto').then(m => m.DetalleProducto) },
      { path: 'iniciar-sesion', loadComponent: () => import('./pages/inicio-sesion/inicio-sesion').then(m => m.InicioSesion) },
      { path: 'registro', loadComponent: () => import('./pages/registro/registro').then(m => m.Registro) },
      { path: 'carrito', loadComponent: () => import('./pages/carrito/carrito').then(m => m.Carrito) },
      { path: 'checkout', loadComponent: () => import('./pages/checkout/checkout').then(m => m.Checkout), canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'pedido-confirmado/:id', loadComponent: () => import('./pages/confirmacion-pedido/confirmacion-pedido').then(m => m.ConfirmacionPedido), canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'mis-pedidos', loadComponent: () => import('./pages/mis-pedidos/mis-pedidos').then(m => m.MisPedidos), canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'mis-pedidos/:id', loadComponent: () => import('./pages/detalle-pedido/detalle-pedido').then(m => m.DetallePedido) , canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'acceso-denegado', loadComponent: () => import('./pages/acceso-denegado/acceso-denegado').then(m => m.AccesoDenegado) },
      { path: '**', loadComponent: () => import('./pages/pagina-no-encontrada/pagina-no-encontrada').then(m => m.PaginaNoEncontrada) },
    ]
  }
];
