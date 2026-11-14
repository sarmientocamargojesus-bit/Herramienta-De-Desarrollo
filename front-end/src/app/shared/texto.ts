// Normaliza un texto para buscar sin distinguir mayúsculas ni tildes: "Limonada Iqueña" coincide con "iquena".
export function normalizar(texto: string | null | undefined): string {
  return (texto ?? '').normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase().trim();
}

export function coincide(busqueda: string, ...campos: (string | number | null | undefined)[]): boolean {
  const termino = normalizar(busqueda);
  return !termino || campos.some(campo => normalizar(String(campo ?? '')).includes(termino));
}
