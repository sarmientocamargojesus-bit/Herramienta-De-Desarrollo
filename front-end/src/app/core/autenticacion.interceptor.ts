import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { SesionService } from './sesion.service';

export const autenticacionInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const sesion = inject(SesionService);
  const token = sesion.token();
  if (!token || !peticion.url.startsWith('/api/')) return siguiente(peticion);
  return siguiente(peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })).pipe(
    // Un 401 con token significa que el backend ya no lo acepta (vencido, cuenta desactivada o rol cambiado).
    catchError(error => {
      if (error instanceof HttpErrorResponse && error.status === 401) sesion.salir();
      return throwError(() => error);
    })
  );
};
