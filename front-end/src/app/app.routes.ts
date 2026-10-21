import { Routes } from '@angular/router';
import { rolGuard } from './core/rol.guard';

export const routes: Routes = [
  {
    path: 'gestion',
    loadComponent: () => import('./components/panel-gestion/panel-gestion').then(m => m.PanelGestion),
    canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR', 'EMPLEADO'] },
    children: [
      { path: '', redirectTo: 'pedidos', pathMatch: 'full' },
      { path: 'productos', loadComponent: () => import('./components/gestion-productos/gestion-productos').then(m => m.GestionProductos), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'productos/nuevo', loadComponent: () => import('./components/formulario-producto/formulario-producto').then(m => m.FormularioProducto), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'productos/:id/editar', loadComponent: () => import('./components/formulario-producto/formulario-producto').then(m => m.FormularioProducto), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'categorias', loadComponent: () => import('./components/gestion-categorias/gestion-categorias').then(m => m.GestionCategorias), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'categorias/nueva', loadComponent: () => import('./components/formulario-categoria/formulario-categoria').then(m => m.FormularioCategoria), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'categorias/:id/editar', loadComponent: () => import('./components/formulario-categoria/formulario-categoria').then(m => m.FormularioCategoria), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'inventario', loadComponent: () => import('./components/gestion-inventario/gestion-inventario').then(m => m.GestionInventario) },
      { path: 'inventario/:id/editar', loadComponent: () => import('./components/formulario-inventario/formulario-inventario').then(m => m.FormularioInventario) },
      { path: 'pedidos', loadComponent: () => import('./components/gestion-pedidos/gestion-pedidos').then(m => m.GestionPedidos) },
      { path: 'pedidos/:id', loadComponent: () => import('./components/detalle-pedido/detalle-pedido').then(m => m.DetallePedido) },
      { path: 'pagos', loadComponent: () => import('./components/gestion-pagos/gestion-pagos').then(m => m.GestionPagos), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'usuarios', loadComponent: () => import('./components/gestion-usuarios/gestion-usuarios').then(m => m.GestionUsuarios), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
      { path: 'usuarios/:id/rol', loadComponent: () => import('./components/cambio-rol/cambio-rol').then(m => m.CambioRol), canActivate: [rolGuard], data: { roles: ['ADMINISTRADOR'] } },
    ]
  },
  {
    path: '',
    loadComponent: () => import('./components/diseno-cliente/diseno-cliente').then(m => m.DisenoCliente),
    children: [
      { path: '', loadComponent: () => import('./components/inicio/inicio').then(m => m.Inicio) },
      { path: 'productos/:id', loadComponent: () => import('./components/detalle-producto/detalle-producto').then(m => m.DetalleProducto) },
      { path: 'iniciar-sesion', loadComponent: () => import('./components/inicio-sesion/inicio-sesion').then(m => m.InicioSesion) },
      { path: 'registro', loadComponent: () => import('./components/registro/registro').then(m => m.Registro) },
      { path: 'carrito', loadComponent: () => import('./components/carrito/carrito').then(m => m.Carrito) },
      { path: 'checkout', loadComponent: () => import('./components/checkout/checkout').then(m => m.Checkout), canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'pedido-confirmado/:id', loadComponent: () => import('./components/confirmacion-pedido/confirmacion-pedido').then(m => m.ConfirmacionPedido), canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'mis-pedidos', loadComponent: () => import('./components/mis-pedidos/mis-pedidos').then(m => m.MisPedidos), canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'mis-pedidos/:id', loadComponent: () => import('./components/detalle-pedido/detalle-pedido').then(m => m.DetallePedido) , canActivate: [rolGuard], data: { roles: ['CLIENTE'] } },
      { path: 'acceso-denegado', loadComponent: () => import('./components/acceso-denegado/acceso-denegado').then(m => m.AccesoDenegado) },
      { path: '**', loadComponent: () => import('./components/pagina-no-encontrada/pagina-no-encontrada').then(m => m.PaginaNoEncontrada) },
    ]
  }
];
