import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

// Rechaza textos formados solo por espacios: el backend también los considera vacíos.
export const noSoloEspacios: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;
  return typeof valor === 'string' && valor.length > 0 && !valor.trim() ? { soloEspacios: true } : null;
};

// Validador de grupo: la confirmación debe ser igual al campo original (por ejemplo, contraseñas).
export function camposIguales(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const original = grupo.get(campo)?.value;
    const repetido = grupo.get(confirmacion)?.value;
    return repetido && original !== repetido ? { noCoinciden: true } : null;
  };
}

// Igual que el backend: texto@dominio.ext, sin espacios. Validators.email acepta "ana@correo" sin punto.
const FORMATO_CORREO = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
export const correoConDominio: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = typeof control.value === 'string' ? control.value.trim() : '';
  return valor && !FORMATO_CORREO.test(valor) ? { correoIncompleto: true } : null;
};
