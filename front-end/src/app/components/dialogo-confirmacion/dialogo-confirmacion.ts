import { Component, input, output } from '@angular/core';

@Component({
  imports: [],
  selector: "app-dialogo-confirmacion",
  styleUrl: "./dialogo-confirmacion.scss",
  templateUrl: "./dialogo-confirmacion.html",
})
export class DialogoConfirmacion {
  abierto = input(false);
  titulo = input('Confirmar acción');
  mensaje = input('¿Confirmas esta acción?');
  textoConfirmar = input('Confirmar');
  confirmar = output<void>();
  cancelar = output<void>();
}
