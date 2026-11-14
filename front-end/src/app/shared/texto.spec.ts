import { coincide, normalizar } from './texto';

describe('Búsqueda de texto', () => {
  it('ignora mayúsculas y tildes', () => {
    expect(normalizar('  Limonada IQUEÑA ')).toBe('limonada iquena');
    expect(coincide('iquena', 'Limonada Iqueña')).toBe(true);
    expect(coincide('MARACUYÁ', 'Maracuyá Helado')).toBe(true);
  });

  it('busca en cualquiera de los campos y acepta números', () => {
    expect(coincide('ana@', 'Ana Pérez', 'ana@correo.com')).toBe(true);
    expect(coincide('12', 12, 'Cliente')).toBe(true);
    expect(coincide('pollo', 'Burger Brava', 'Carne')).toBe(false);
  });

  it('una búsqueda vacía muestra todo', () => {
    expect(coincide('   ', 'Cualquier cosa')).toBe(true);
  });
});
