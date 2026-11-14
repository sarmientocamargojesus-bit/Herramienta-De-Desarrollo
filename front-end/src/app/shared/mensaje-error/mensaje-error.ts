import { Component, input, output } from '@angular/core';

@Component({
  imports: [],
  selector: "app-mensaje-error",
  styleUrl: "./mensaje-error.scss",
  templateUrl: "./mensaje-error.html",
})
export class MensajeError {
  texto = input('');
  // Si el error permite repetir la petición, el padre recibe el evento reintentar y vuelve a cargar.
  reintentable = input(false);
  reintentar = output<void>();
}
