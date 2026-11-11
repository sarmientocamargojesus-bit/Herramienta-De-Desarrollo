import { HttpErrorResponse } from '@angular/common/http';

export function mensajeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'No se pudo conectar con el backend. Verifica que Spring Boot esté iniciado.';
    if (error.status === 401) return 'La sesión no es válida o ha vencido. Inicia sesión nuevamente.';
    if (error.status === 403) return 'No tienes permiso para realizar esta acción.';
    const cuerpo = error.error as { mensaje?: string; message?: string } | null;
    const mensaje = cuerpo?.mensaje || cuerpo?.message;
    if (error.status === 404) return mensaje || 'El recurso solicitado no existe o fue eliminado.';
    return mensaje || `La solicitud no pudo completarse (${error.status}).`;
  }
  return error instanceof Error ? error.message : 'Ocurrió un error inesperado.';
}

// Sin conexión o con una falla del servidor tiene sentido repetir la misma petición;
// ante datos inválidos (400), permisos (401/403) o recursos inexistentes (404) no.
export function esReintentable(error: unknown): boolean {
  return error instanceof HttpErrorResponse && (error.status === 0 || error.status >= 500);
}
