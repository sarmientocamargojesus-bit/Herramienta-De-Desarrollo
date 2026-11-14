import { Component, input, output } from '@angular/core';

// Botones − y + para elegir una cantidad. El padre recibe el nuevo valor y decide qué hacer con él.
@Component({
  imports: [],
  selector: 'app-selector-cantidad',
  templateUrl: './selector-cantidad.html',
  styleUrl: './selector-cantidad.scss',
})
export class SelectorCantidad {
  readonly valor = input.required<number>();
  readonly minimo = input(1);
  readonly maximo = input(100);
  readonly etiqueta = input('Cantidad');
  readonly deshabilitado = input(false);
  readonly valorCambio = output<number>();

  cambiar(diferencia: number): void {
    const nuevo = this.valor() + diferencia;
    if (nuevo >= this.minimo() && nuevo <= this.maximo()) this.valorCambio.emit(nuevo);
  }
}
