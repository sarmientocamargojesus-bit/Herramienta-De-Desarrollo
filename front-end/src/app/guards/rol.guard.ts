import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/product';
import { SesionService } from '../services/sesion.service';

export const rolGuard: CanActivateFn = (ruta, estado) => {
  const sesion = inject(SesionService);
  const router = inject(Router);
  if (!sesion.autenticado()) return router.createUrlTree(['/iniciar-sesion'], { queryParams: { volver: estado.url } });
  const permitidos = ruta.data['roles'] as Rol[];
  return permitidos.includes(sesion.rol() as Rol) ? true : router.createUrlTree(['/acceso-denegado']);
};
