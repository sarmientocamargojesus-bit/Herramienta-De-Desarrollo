import { HttpErrorResponse } from '@angular/common/http';

export function mensajeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'No se pudo conectar con el backend. Verifica que Spring Boot esté iniciado.';
    if (error.status === 401) return 'La sesión no es válida o ha vencido. Inicia sesión nuevamente.';
    if (error.status === 403) return 'No tienes permiso para realizar esta acción.';
    const cuerpo = error.error as { mensaje?: string; message?: string } | null;
    return cuerpo?.mensaje || cuerpo?.message || `La solicitud no pudo completarse (${error.status}).`;
  }
  return error instanceof Error ? error.message : 'Ocurrió un error inesperado.';
}
