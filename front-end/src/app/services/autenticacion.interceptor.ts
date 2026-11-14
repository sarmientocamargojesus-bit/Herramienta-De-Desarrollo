import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_URL } from './api.config';
import { SesionService } from './sesion.service';

export const autenticacionInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const sesion = inject(SesionService);
  const api = inject(API_URL);
  const token = sesion.token();
  // El token solo se envía a la API propia, nunca a otros sitios.
  if (!token || !peticion.url.startsWith(`${api}/`)) return siguiente(peticion);
  return siguiente(peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })).pipe(
    // Un 401 con token significa que el backend ya no lo acepta (vencido, cuenta desactivada o rol cambiado).
    // Se cierra la sesión y se repite una sola vez sin token: las rutas públicas, como el menú, responden
    // con normalidad y las protegidas vuelven a responder 401 para pedir un nuevo inicio de sesión.
    catchError(error => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) return throwError(() => error);
      sesion.salir();
      return siguiente(peticion);
    })
  );
};
