import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AvisosService } from '../../services/avisos.service';

@Component({
  imports: [RouterLink],
  selector: 'app-aviso-exito',
  templateUrl: './aviso-exito.html',
  styleUrl: './aviso-exito.scss',
})
export class AvisoExito {
  readonly avisos = inject(AvisosService);
}
