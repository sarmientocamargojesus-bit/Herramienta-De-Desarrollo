import { Component } from "@angular/core";
import { RouterOutlet } from '@angular/router';
import { Encabezado } from '../encabezado/encabezado';
import { PiePagina } from '../pie-pagina/pie-pagina';

@Component({
  imports: [RouterOutlet, Encabezado, PiePagina],
  selector: "app-diseno-cliente",
  styleUrl: "./diseno-cliente.scss",
  templateUrl: "./diseno-cliente.html",
})
export class DisenoCliente {}
