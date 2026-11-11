import { TestBed } from '@angular/core/testing';
import { AvisosService } from './avisos.service';

describe('AvisosService', () => {
  afterEach(() => vi.useRealTimers());

  it('muestra el aviso y lo oculta después de unos segundos', () => {
    vi.useFakeTimers();
    const avisos = TestBed.inject(AvisosService);

    avisos.exito('Producto guardado.');
    expect(avisos.mensaje()).toBe('Producto guardado.');

    vi.advanceTimersByTime(4000);
    expect(avisos.mensaje()).toBe('');
  });

  it('un aviso nuevo reemplaza al anterior y reinicia el tiempo', () => {
    vi.useFakeTimers();
    const avisos = TestBed.inject(AvisosService);

    avisos.exito('Primero');
    vi.advanceTimersByTime(3000);
    avisos.exito('Segundo');
    vi.advanceTimersByTime(3000);
    expect(avisos.mensaje()).toBe('Segundo');
  });

  it('se puede cerrar manualmente', () => {
    const avisos = TestBed.inject(AvisosService);
    avisos.exito('Rol actualizado.');
    avisos.cerrar();
    expect(avisos.mensaje()).toBe('');
  });
});
