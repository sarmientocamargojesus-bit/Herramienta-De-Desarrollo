import { HttpErrorResponse } from '@angular/common/http';
import { esReintentable, mensajeError } from './errores';

const respuesta = (status: number, error: unknown = null) => new HttpErrorResponse({ status, error });

describe('Manejo de errores HTTP', () => {
  it('explica un 404 sin mensaje del backend', () => {
    expect(mensajeError(respuesta(404))).toBe('El recurso solicitado no existe o fue eliminado.');
  });

  it('usa el mensaje del backend cuando lo envía', () => {
    expect(mensajeError(respuesta(404, { mensaje: 'Producto no encontrado: 7' }))).toBe('Producto no encontrado: 7');
    expect(mensajeError(respuesta(409, { mensaje: 'Stock insuficiente' }))).toBe('Stock insuficiente');
  });

  it('explica que el servidor no está disponible ante 502, 503 y 504', () => {
    for (const status of [502, 503, 504]) {
      expect(mensajeError(respuesta(status))).toBe(
        'El servidor no está disponible en este momento. Intenta de nuevo en unos segundos.');
    }
  });

  it('solo permite reintentar sin conexión o ante fallas del servidor', () => {
    expect(esReintentable(respuesta(0))).toBe(true);
    expect(esReintentable(respuesta(500))).toBe(true);
    expect(esReintentable(respuesta(502))).toBe(true);
    for (const status of [400, 401, 403, 404, 409]) {
      expect(esReintentable(respuesta(status))).toBe(false);
    }
    expect(esReintentable(new Error('local'))).toBe(false);
  });
});
