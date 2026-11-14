import { Component } from "@angular/core";
import { RouterOutlet } from '@angular/router';
import { Encabezado } from '../../components/encabezado/encabezado';
import { PiePagina } from '../../components/pie-pagina/pie-pagina';
import { AvisoExito } from '../../shared/aviso-exito/aviso-exito';

@Component({
  imports: [RouterOutlet, Encabezado, PiePagina, AvisoExito],
  selector: "app-diseno-cliente",
  styleUrl: "./diseno-cliente.scss",
  templateUrl: "./diseno-cliente.html",
})
export class DisenoCliente {}
