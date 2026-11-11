import { TestBed } from '@angular/core/testing';
import { EtiquetaEstado } from './etiqueta-estado';

describe('EtiquetaEstado', () => {
  function etiqueta(valor: string): HTMLElement {
    const fixture = TestBed.createComponent(EtiquetaEstado);
    fixture.componentRef.setInput('valor', valor);
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).querySelector('.etiqueta')!;
  }

  it.each([
    ['CONFIRMADO', 'exito', 'Confirmado'],
    ['APROBADO', 'exito', 'Aprobado'],
    ['PENDIENTE', 'alerta', 'Pendiente'],
    ['STOCK BAJO', 'alerta', 'Stock bajo'],
    ['CANCELADO', 'peligro', 'Cancelado'],
    ['AGOTADO', 'peligro', 'Agotado'],
    ['ADMINISTRADOR', 'admin', 'Administrador'],
    ['EMPLEADO', 'info', 'Empleado'],
    ['CLIENTE', 'neutro', 'Cliente'],
  ])('%s se muestra en tono %s como "%s"', (valor, tono, texto) => {
    const elemento = etiqueta(valor);
    expect(elemento.classList).toContain(tono);
    expect(elemento.textContent).toBe(texto);
  });

  it('un valor desconocido se muestra en gris', () => {
    expect(etiqueta('EN_REVISION').classList).toContain('neutro');
  });
});
