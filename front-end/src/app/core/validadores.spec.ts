import { FormControl, FormGroup } from '@angular/forms';
import { camposIguales, noSoloEspacios } from './validadores';

describe('Validadores personalizados', () => {
  it('noSoloEspacios rechaza un texto formado solo por espacios', () => {
    expect(noSoloEspacios(new FormControl('   '))).toEqual({ soloEspacios: true });
    expect(noSoloEspacios(new FormControl(' Ana '))).toBeNull();
    expect(noSoloEspacios(new FormControl(''))).toBeNull(); // El vacío lo informa required.
  });

  it('camposIguales marca el grupo cuando la confirmación es distinta', () => {
    const grupo = new FormGroup(
      { contrasena: new FormControl('secreta1'), confirmarContrasena: new FormControl('secreta2') },
      { validators: camposIguales('contrasena', 'confirmarContrasena') }
    );
    expect(grupo.errors).toEqual({ noCoinciden: true });

    grupo.controls.confirmarContrasena.setValue('secreta1');
    expect(grupo.errors).toBeNull();
  });
});
